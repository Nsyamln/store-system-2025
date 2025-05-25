package tokoibuelin.storesystem.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Repository
public class ProfitSharingRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
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
