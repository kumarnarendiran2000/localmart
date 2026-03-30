using Microsoft.EntityFrameworkCore;
using payment_service.Data;
using payment_service.DTOs;
using payment_service.Models;

namespace payment_service.Services;

// Business logic — processes payments and returns the outcome.
// Injected into KafkaConsumerService (which calls ProcessPaymentAsync after consuming an event).
// Injected into PaymentsController (which calls GetPaymentByOrderIdAsync for the GET endpoint).
public class PaymentService
{
    private readonly PaymentDbContext _context;
    private readonly ILogger<PaymentService> _logger;

    // Constructor injection — same concept as Spring's @RequiredArgsConstructor.
    // No annotation needed in .NET — the DI container resolves constructor params automatically.
    public PaymentService(PaymentDbContext context, ILogger<PaymentService> logger)
    {
        _context = context;
        _logger = logger;
    }

    // Called by KafkaConsumerService when an order.placed event arrives.
    // Returns the outcome event to be published back to payment.processed topic.
    public async Task<PaymentProcessedEvent> ProcessPaymentAsync(OrderPlacedEvent order)
    {
        // Create a PENDING payment record first — we have a record of the attempt regardless of outcome.
        var payment = new Payment
        {
            OrderId = order.OrderId
        };
        _context.Payments.Add(payment);
        await _context.SaveChangesAsync();

        _logger.LogInformation("Processing payment for orderId={OrderId}, amount={Amount}",
            order.OrderId, order.TotalAmount);

        // Simulate payment processing — 80% success, 20% failure.
        // Real implementation: call payment gateway (Razorpay, Stripe, etc.) here.
        var isSuccess = Random.Shared.NextDouble() > 0.2;

        if (isSuccess)
        {
            payment.Status = PaymentStatus.Success;
            payment.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            _logger.LogInformation("Payment SUCCESS for orderId={OrderId}", order.OrderId);
            return new PaymentProcessedEvent(order.OrderId, "SUCCESS", null);
        }
        else
        {
            payment.Status = PaymentStatus.Failed;
            payment.Reason = "Insufficient funds";
            payment.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            _logger.LogWarning("Payment FAILED for orderId={OrderId}", order.OrderId);
            return new PaymentProcessedEvent(order.OrderId, "FAILED", "Insufficient funds");
        }
    }

    // Called by PaymentsController — look up payment status by orderId.
    public async Task<Payment?> GetPaymentByOrderIdAsync(Guid orderId)
    {
        return await _context.Payments.FirstOrDefaultAsync(p => p.OrderId == orderId);
    }
}
