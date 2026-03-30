using System.Text.Json;
using Confluent.Kafka;
using payment_service.DTOs;
using payment_service.Services;

namespace payment_service.Kafka;

// IHostedService = a background service that runs for the lifetime of the application.
// .NET equivalent of Spring's @KafkaListener — but Spring hides the threading for you.
// Here we manage it explicitly: Start() begins the consumer loop, StopAsync() cancels it.
//
// Why IHostedService and not a controller?
//   Kafka consuming is not triggered by an HTTP request — it runs continuously in the background.
//   Controllers handle HTTP. Background workers handle everything else.
//   Spring Boot does the same thing internally — @KafkaListener runs in a background thread pool.
public class OrderEventConsumer : IHostedService
{
    private readonly IConsumer<string, string> _consumer;
    private readonly IProducer<string, string> _producer;
    private readonly IServiceScopeFactory _scopeFactory;
    private readonly ILogger<OrderEventConsumer> _logger;
    private readonly IConfiguration _configuration;
    private CancellationTokenSource _cts = new();
    private Task _consumerTask = Task.CompletedTask;

    // Why IServiceScopeFactory instead of injecting PaymentService directly?
    //   PaymentService uses PaymentDbContext (EF Core) which is registered as "Scoped" —
    //   a new instance per HTTP request (or per manually created scope).
    //   IHostedService is a Singleton — it lives for the app's entire lifetime.
    //   A Singleton cannot directly hold a Scoped dependency (it would outlive its intended scope).
    //   Solution: inject IServiceScopeFactory and create a fresh scope per message.
    //   This is the standard .NET pattern for using scoped services inside background workers.
    //   Spring handles this automatically — here we do it explicitly.
    public OrderEventConsumer(
        IConsumer<string, string> consumer,
        IProducer<string, string> producer,
        IServiceScopeFactory scopeFactory,
        ILogger<OrderEventConsumer> logger,
        IConfiguration configuration)
    {
        _consumer = consumer;
        _producer = producer;
        _scopeFactory = scopeFactory;
        _logger = logger;
        _configuration = configuration;
    }

    // Called by .NET when the app starts — equivalent of Spring starting @KafkaListener threads.
    public Task StartAsync(CancellationToken cancellationToken)
    {
        _consumer.Subscribe("order.placed");
        _logger.LogInformation("OrderEventConsumer subscribed to order.placed topic");

        // Run the consume loop in a background thread — fire and forget.
        // Task.Run = start this on a thread pool thread, don't await it here.
        _consumerTask = Task.Run(() => ConsumeLoop(_cts.Token), cancellationToken);
        return Task.CompletedTask;
    }

    // Called by .NET when the app is shutting down — graceful shutdown.
    public async Task StopAsync(CancellationToken cancellationToken)
    {
        _logger.LogInformation("OrderEventConsumer stopping...");
        _cts.Cancel();              // signal the consume loop to stop
        await _consumerTask;        // wait for it to finish its current message
        _consumer.Close();          // commits final offsets, notifies Kafka this consumer is leaving
    }

    // The actual consume loop — runs continuously in the background.
    private async Task ConsumeLoop(CancellationToken cancellationToken)
    {
        while (!cancellationToken.IsCancellationRequested)
        {
            try
            {
                // Consume() blocks until a message arrives or timeout.
                // TimeSpan.FromSeconds(1) = poll timeout — how long to wait before returning null.
                // This allows the cancellation check in the while loop to run periodically.
                var result = _consumer.Consume(TimeSpan.FromSeconds(1));
                if (result == null) continue; // timeout — no message, loop again

                _logger.LogDebug("Received order.placed message: {Message}", result.Message.Value);

                OrderPlacedEvent? order;
                try
                {
                    order = JsonSerializer.Deserialize<OrderPlacedEvent>(
                        result.Message.Value,
                        new JsonSerializerOptions { PropertyNameCaseInsensitive = true }
                    );
                }
                catch (Exception ex)
                {
                    _logger.LogError("Failed to deserialize order.placed message — skipping. Error: {Error}", ex.Message);
                    continue;
                }

                if (order == null)
                {
                    _logger.LogWarning("Deserialized order.placed event is null — skipping");
                    continue;
                }

                // Create a fresh DI scope for this message — gives us a fresh PaymentDbContext.
                // Equivalent of Spring's per-message transaction scope in @KafkaListener.
                using var scope = _scopeFactory.CreateScope();
                var paymentService = scope.ServiceProvider.GetRequiredService<PaymentService>();

                // Process payment and get the outcome event.
                var outcomeEvent = await paymentService.ProcessPaymentAsync(order);

                // Publish payment.processed to Kafka — order-service consumes this.
                // Use orderId as key — same partition ordering guarantee as on the Java side.
                var outcomeJson = JsonSerializer.Serialize(outcomeEvent,
                    new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase });

                var outputTopic = _configuration["Kafka:OutputTopic"] ?? "payment.processed";
                await _producer.ProduceAsync(outputTopic, new Message<string, string>
                {
                    Key = order.OrderId.ToString(),
                    Value = outcomeJson
                });

                _logger.LogInformation("Published payment.processed for orderId={OrderId} status={Status}",
                    order.OrderId, outcomeEvent.Status);
            }
            catch (OperationCanceledException)
            {
                // Normal shutdown — not an error.
                break;
            }
            catch (Exception ex)
            {
                _logger.LogError("Unexpected error in OrderEventConsumer: {Error}", ex.Message);
                // Brief pause before retrying — avoids tight error loop hammering resources.
                await Task.Delay(1000, cancellationToken);
            }
        }
    }
}
