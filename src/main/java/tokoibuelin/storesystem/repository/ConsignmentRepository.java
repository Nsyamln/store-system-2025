package tokoibuelin.storesystem.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tokoibuelin.storesystem.entity.Consignment;

@Repository
public class ConsignmentRepository {
    private static final Logger log = LoggerFactory.getLogger(ConsignmentRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public ConsignmentRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;

    }


    public long saveConsignment(List<Consignment> consignment) {
       String sqlDetail = "INSERT INTO consignments (sale_id, order_id, product_id, supplier_id, product_quantity, total_purchase_price, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            for (Consignment consign : consignment) {
                jdbcTemplate.update(sqlDetail, consign.saleId(), consign.orderId(), consign.productId(), consign.supplierId(), consign.productQuantity(), consign.totalPrice(), consign.status());
            }
            return 1L;
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    public Long sumSharing(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, formatter);

        Timestamp startTimestamp = Timestamp.valueOf(startLocalDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endLocalDate.atTime(LocalTime.MAX));

        System.out.println("Start Timestamp3: " + startTimestamp);
        System.out.println("End Timestamp3: " + endTimestamp);
        try {
            String sql = "SELECT SUM(total_purchase_price) AS total FROM profit_sharing WHERE payment_date BETWEEN ? AND ? ";
            Long total = jdbcTemplate.queryForObject(sql,new Object[]{startTimestamp, endTimestamp},  Long.class);
            System.out.println("total Sharing : " + total);
            return total != null ? total : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

}
