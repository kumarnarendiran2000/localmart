using Microsoft.EntityFrameworkCore;
using payment_service.Models;

namespace payment_service.Data;

// DbContext is the EF Core equivalent of Spring Data JPA's combination of:
//   - EntityManagerFactory (manages the DB connection + session)
//   - JpaRepository (gives you Save, FindById, etc.)
//
// You define one DbContext per service. It holds DbSet<T> properties —
// one per entity/table. EF Core uses these to generate migrations and run queries.
public class PaymentDbContext : DbContext
{
    // Constructor — receives DbContextOptions injected by the DI container.
    // Like Spring Boot injecting DataSource into JPA auto-configuration.
    public PaymentDbContext(DbContextOptions<PaymentDbContext> options) : base(options)
    {
    }

    // DbSet<Payment> = the "payments" table.
    // Like JpaRepository<Payment, Guid> — gives you Payments.Add(), Payments.FindAsync(), etc.
    // EF Core sees this property and knows to create/manage a "payments" table.
    public DbSet<Payment> Payments { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // Store PaymentStatus enum as its string name in DB (e.g. "Pending", "Success", "Failed")
        // instead of an integer (0, 1, 2).
        // Like @Enumerated(EnumType.STRING) in JPA.
        modelBuilder.Entity<Payment>()
            .Property(p => p.Status)
            .HasConversion<string>();
    }
}
