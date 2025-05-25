package tokoibuelin.storesystem.model.response;

import tokoibuelin.storesystem.entity.Order;
import tokoibuelin.storesystem.entity.OrderDetails;
import tokoibuelin.storesystem.entity.Sale;
import tokoibuelin.storesystem.entity.SaleDetails;

import java.time.OffsetDateTime;
import java.util.List;

public record OrderDto(
        String orderId,
        String orderDate,
        String customerId,
        String customerName,
        String deliveryAddress,
        String phone,
        Order.Status status,
        String createdBy,
        String updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Integer shippingCost,
        String trackingNumber,
        String courier,
        String shippingMethod,
        OffsetDateTime estimatedDeliveryDate,
        OffsetDateTime actualDeliveryDate,
        List<OrderDetails> orderDetails

) {
}
