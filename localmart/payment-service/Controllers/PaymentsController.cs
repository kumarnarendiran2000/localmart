using Microsoft.AspNetCore.Mvc;
using payment_service.Services;

namespace payment_service.Controllers;

// [ApiController]  = tells ASP.NET this is a REST API controller.
//                    Enables automatic model validation, proper 400 responses, etc.
//                    Equivalent of @RestController in Spring Boot.
//
// [Route("payments")] = base URL path for all endpoints in this controller.
//                       Equivalent of @RequestMapping("/payments") on the class.
[ApiController]
[Route("payments")]
public class PaymentsController : ControllerBase
{
    private readonly PaymentService _paymentService;
    private readonly ILogger<PaymentsController> _logger;

    public PaymentsController(PaymentService paymentService, ILogger<PaymentsController> logger)
    {
        _paymentService = paymentService;
        _logger = logger;
    }

    // GET /payments/{orderId}
    // Returns the payment record for a given orderId.
    // Equivalent of @GetMapping("/{orderId}") in Spring Boot.
    [HttpGet("{orderId:guid}")]
    public async Task<IActionResult> GetByOrderId(Guid orderId)
    {
        _logger.LogDebug("GET /payments/{OrderId}", orderId);

        var payment = await _paymentService.GetPaymentByOrderIdAsync(orderId);

        if (payment == null)
        {
            // Equivalent of throwing ResourceNotFoundException → GlobalExceptionHandler returning 404.
            // In .NET we return NotFound() directly — no exception needed for simple cases.
            return NotFound(new { message = $"No payment found for orderId: {orderId}" });
        }

        return Ok(new
        {
            payment.Id,
            payment.OrderId,
            Status = payment.Status.ToString(),
            payment.Reason,
            payment.CreatedAt,
            payment.UpdatedAt
        });
    }
}
