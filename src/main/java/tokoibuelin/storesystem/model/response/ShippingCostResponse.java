package tokoibuelin.storesystem.model.response;

public record ShippingCostResponse(
        String service,
        int cost,
        String estimatedDelivery,
        java.time.OffsetDateTime estimatedDeliveryDate) {
}
