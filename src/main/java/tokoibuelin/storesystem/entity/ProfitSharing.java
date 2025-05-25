package tokoibuelin.storesystem.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProfitSharing(
        String profitSharingId,
        String saleId,
        String productId,
        String supplierId,
        int productQuantity,
        BigDecimal totalPurchasePrice,
        BigDecimal totalSalePrice,
        String status,
        OffsetDateTime paymentDate
) {
//    public enum Status {
//        PENDING, PAID;
//
//        public static ProfitSharing.Status fromString(String str) {
//            if (PENDING.name().equals(str)) {
//                return PENDING;
//            }  else {
//                return PAID;
//            }
//        }
//
//    }
}

