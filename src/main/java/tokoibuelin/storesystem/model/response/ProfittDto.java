package tokoibuelin.storesystem.model.response;

import java.math.BigDecimal;

public record ProfittDto(
        String profitSharingId,
        String saleId,
        String productId,
        String supplierId,
        int productQuantity,
        BigDecimal totalPurchasePrice,
        BigDecimal totalSalePrice,
        String status,
        String paymentDate,
        String productName
) {
}
