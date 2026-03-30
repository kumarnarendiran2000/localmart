namespace payment_service.DTOs;

// Matches exactly the Java record OrderPlacedEvent published by order-service to "order.placed" topic.
// Field names must match JSON keys in the Java record — C# and Java both serialize to camelCase by default.
//
// C# record = Java record. Immutable, auto-generates equality, ToString.
// { get; init; } = property that can only be set during object construction (immutable after).
public record OrderPlacedEvent(
    Guid OrderId,
    Guid UserId,
    string ShopId,
    string ProductId,
    string ProductName,
    decimal UnitPrice,
    int Quantity,
    decimal TotalAmount
);
