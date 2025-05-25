package tokoibuelin.storesystem.model.request;

import tokoibuelin.storesystem.model.response.OrderDetailDto;
import java.math.BigDecimal;
import java.util.List;

public record OnlineSaleReq (
    String customerId,
    String deliveryAddress,
    String phone,
    BigDecimal totalPrice,
    String paymentMethod,
    BigDecimal amountPaid,
    String courier,
    Integer shippingCost,
    String shippingMethod,
    String estimatedDelivery,
    List<OrderDetailDto> orderDetail
){
}
