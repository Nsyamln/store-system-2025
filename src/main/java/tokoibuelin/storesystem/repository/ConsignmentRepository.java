package tokoibuelin.storesystem.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import tokoibuelin.storesystem.entity.Consignment;
import tokoibuelin.storesystem.entity.OrderDetails;

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

    // public String saveConsignment(final Consignment consignment) {
    //     final KeyHolder keyHolder = new GeneratedKeyHolder();
    //     try {
    //         int updateCount = jdbcTemplate.update(con -> {
    //             PreparedStatement ps = consignment.insert(con);
    //             return ps;
    //         }, keyHolder);

    //         if (updateCount != 1) {
    //             return null; // atau return 0L sesuai kebutuhan
    //         }

    //         // Ambil hasil dari KeyHolder sebagai String
    //         Map<String, Object> keys = keyHolder.getKeys();
    //         if (keys != null && keys.containsKey("consignment_id")) {
    //             return (String) keys.get("consignment_id");
    //         }

    //         return null;
    //     } catch (Exception e) {
    //         log.error("Error during saveConsignment: {}", e.getMessage());
    //         return null; 
    //     }
    // }

}
