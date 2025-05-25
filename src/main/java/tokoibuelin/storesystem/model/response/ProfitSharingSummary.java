package tokoibuelin.storesystem.model.response;

import java.math.BigDecimal;

public record ProfitSharingSummary(
            String profitSharingId,
            String saleId,
            String productId,
            String supplierId,
            int productQuantity,
            BigDecimal totalPurchasePrice

        ) {
}
