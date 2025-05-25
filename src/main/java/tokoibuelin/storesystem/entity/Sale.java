package tokoibuelin.storesystem.entity;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


public record Sale(
        String saleId,
        OffsetDateTime saleDate,
        BigDecimal totalPrice,
        BigDecimal amountPaid,
        String paymentMethod
) {

    public static final String TABLE_NAME = "sales";

    public PreparedStatement insert(final Connection connection) {
        try {
            final String sql = "INSERT INTO " + TABLE_NAME + " (sale_date, total_price,amount_paid,payment_method) VALUES (?, ?, ?, ?)";
            final PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setTimestamp(1, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()));
            ps.setBigDecimal(2, totalPrice);
            ps.setBigDecimal(3, amountPaid);
            ps.setString(4, paymentMethod());

            return ps;
        } catch (Exception e) {
            System.out.println("Error during saveOrder: {}" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

//    public enum PaymentMethod {
//        BANK_BRI, SHOPEEPAY, DANA, CASH;
//
//        public static PaymentMethod fromString(String str) {
//            return Arrays.stream(PaymentMethod.values())
//                    .filter(method -> method.name().equalsIgnoreCase(str))
//                    .findFirst()
//                    .orElse(CASH);
//        }
//
//    }
}

