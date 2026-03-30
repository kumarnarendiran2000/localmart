using System.ComponentModel.DataAnnotations;

namespace payment_service.Models;

// EF Core entity — maps to the "payments" table in PostgreSQL.
// EF Core uses this class to:
//   1. Generate the migration (CREATE TABLE payments ...)
//   2. Map query results back to C# objects (like JPA @Entity)
//
// No @Entity annotation needed — EF Core discovers entities via DbContext (explained in PaymentDbContext).
// No @Id annotation — EF Core convention: property named "Id" is automatically the primary key.
public class Payment
{
    // Primary key — EF Core convention: property named "Id" = primary key.
    // Like @Id + @GeneratedValue in JPA.
    public Guid Id { get; set; } = Guid.NewGuid();

    // The order this payment is for — comes from the order.placed Kafka event.
    [Required]
    public Guid OrderId { get; set; }

    public PaymentStatus Status { get; set; } = PaymentStatus.Pending;

    // Failure reason — null on success, populated on failure.
    public string? Reason { get; set; }

    // Timestamps — set in code, not by the DB.
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? UpdatedAt { get; set; }
}
