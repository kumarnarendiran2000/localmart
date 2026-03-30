using Confluent.Kafka;
using Microsoft.EntityFrameworkCore;
using payment_service.Data;
using payment_service.Kafka;
using payment_service.Services;

var builder = WebApplication.CreateBuilder(args);

var kafkaBootstrapServers = builder.Configuration["Kafka:BootstrapServers"]!;
var consumerGroupId = builder.Configuration["Kafka:ConsumerGroupId"]!;

// ── Database ───────────────────────────────────────────────────────────────────
// Register PaymentDbContext with the DI container.
// Equivalent of Spring Boot's spring.datasource auto-configuration, but explicit here.
// "Scoped" lifetime (default for AddDbContext) = one instance per HTTP request,
// or per manually created scope (which is what OrderEventConsumer does per message).
builder.Services.AddDbContext<PaymentDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

// ── Kafka Producer ─────────────────────────────────────────────────────────────
// Registered as Singleton — one shared producer for the app's lifetime.
// Kafka producers are thread-safe and expensive to create — always singleton.
// Equivalent of Spring Boot's KafkaTemplate bean (also singleton).
builder.Services.AddSingleton<IProducer<string, string>>(_ =>
{
    var config = new ProducerConfig
    {
        BootstrapServers = kafkaBootstrapServers
    };
    return new ProducerBuilder<string, string>(config).Build();
});

// ── Kafka Consumer ─────────────────────────────────────────────────────────────
// Registered as Singleton — one consumer instance, runs for the app's lifetime.
// Equivalent of Spring's @KafkaListener background thread.
builder.Services.AddSingleton<IConsumer<string, string>>(_ =>
{
    var config = new ConsumerConfig
    {
        BootstrapServers = kafkaBootstrapServers,
        GroupId = consumerGroupId,
        AutoOffsetReset = AutoOffsetReset.Earliest,   // same as auto-offset-reset: earliest in Java
        EnableAutoCommit = true                        // Kafka auto-commits offsets after consume
    };
    return new ConsumerBuilder<string, string>(config).Build();
});

// ── Services ───────────────────────────────────────────────────────────────────
// PaymentService is Transient — a new instance per injection.
// It holds no state — safe to create fresh each time.
// Equivalent of Spring's @Service (default singleton in Spring, but transient is safe here
// since we create it inside a scope per message in the consumer).
builder.Services.AddTransient<PaymentService>();

// ── Background Worker ──────────────────────────────────────────────────────────
// Registers OrderEventConsumer as a hosted service — .NET starts it on app startup
// and stops it on shutdown. Equivalent of Spring auto-starting @KafkaListener threads.
builder.Services.AddHostedService<OrderEventConsumer>();

// ── Controllers + OpenAPI ──────────────────────────────────────────────────────
builder.Services.AddControllers();
builder.Services.AddOpenApi();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

await app.RunAsync();
