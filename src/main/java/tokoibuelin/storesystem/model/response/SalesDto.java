package tokoibuelin.storesystem.model.response;

import tokoibuelin.storesystem.entity.SaleDetails;
import java.util.List;
import java.math.BigDecimal;

public record SalesDto(String saleId,
                      String saleDate,
                      BigDecimal totalPrice,
                      String customerId,
                      String orderId,
                      BigDecimal amountPaid,
                      String paymentMethod,

                      List<SaleDetails> saleDetails) {
    public SalesDto(String saleId, String saleDate, BigDecimal totalPrice, String customerId,
                   String orderId, BigDecimal amountPaid, String paymentMethod,
                   List<SaleDetails> saleDetails) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.totalPrice = totalPrice;
        this.customerId = customerId;
        this.orderId = orderId;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.saleDetails = saleDetails;
    }
    public List<SaleDetails> getSaleDetails() {
        return saleDetails;
    }
}
