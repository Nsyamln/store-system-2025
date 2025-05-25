package tokoibuelin.storesystem.model.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProfitDto(
        String profitSharingId,
                        String saleId,
                        String productId,
                        String supplierId,
                        int productQuantity,
                        BigDecimal totalPurchasePrice,
                        String status,
                        OffsetDateTime paymentDate
                        
) {
}
