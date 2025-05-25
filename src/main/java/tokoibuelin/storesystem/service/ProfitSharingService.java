package tokoibuelin.storesystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tokoibuelin.storesystem.entity.ProfitSharing;
import tokoibuelin.storesystem.model.response.ProfitDto;
import tokoibuelin.storesystem.model.response.ProfittDto;
import tokoibuelin.storesystem.model.response.ProfitSharingSummary;
import tokoibuelin.storesystem.model.response.StockProductDto;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfitSharingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getPendingPaymentsSummaryBySupplier( final String supplierId) {

            String query = "SELECT * FROM consignments  WHERE status = 'UNPAID' AND supplier_id = ?";

            List<ProfitDto> pendingPayments = jdbcTemplate.query(query, new Object[]{supplierId},
                    (rs, rowNum) -> new ProfitDto(
                            rs.getString("consignment_id"),
                            rs.getString("sale_id"),
                            rs.getString("product_id"),
                            rs.getString("supplier_id"),
                            rs.getInt("product_quantity"),
                            rs.getBigDecimal("total_purchase_price"),
                            rs.getString("status"),
                            rs.getObject("payment_date", OffsetDateTime.class)
                    ));

            double totalAmountToPay = pendingPayments.stream()
                    .mapToDouble(payment -> payment.totalPurchasePrice().doubleValue())
                    .sum();

            List<ProfitSharingSummary> summaries = pendingPayments.stream().map(payment -> new ProfitSharingSummary(
                    payment.profitSharingId(),
                    payment.saleId(),
                    payment.productId(),
                    payment.supplierId(),
                    payment.productQuantity(),
                    payment.totalPurchasePrice()
            )).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("totalAmountToPay", totalAmountToPay);
            result.put("details", summaries);

            return result;

    }

    public List<ProfitDto> getAll(String suppId){
        Map<String, ProfitDto> profitMap = new LinkedHashMap<>();

        jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM consignments WHERE supplier_id = ?"
            );
            ps.setString(1, suppId);
            return ps;
        }, (rs, rowNum) -> {
            final String sharingIdFromDb = rs.getString("consignment_id");
            ProfitDto profitDto = profitMap.get(sharingIdFromDb);
            if (profitDto == null) {
                final String saleId = rs.getString("sale_id");
                final String productId = rs.getString("product_id");
                final String supplierId = rs.getString("supplier_id");
                final int productQuantity = rs.getInt("product_quantity");
                final BigDecimal totalPurchasePrice = rs.getBigDecimal("total_purchase_price");
                final String status = rs.getString("status");
                final OffsetDateTime paymentDate = rs.getTimestamp("payment_date") == null ? null
                        : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);

                profitDto = new ProfitDto(sharingIdFromDb,saleId, productId, supplierId, productQuantity, totalPurchasePrice,status, paymentDate);
                profitMap.put(sharingIdFromDb, profitDto);
            }

            return null;
        });

        return new ArrayList<>(profitMap.values());
    }

    @Transactional
    public void updatePaymentStatus(String supplierId) {
        String selectQuery = "SELECT * FROM consignments WHERE status = 'UNPAID' AND supplier_id = ?";
        String updateQuery = "UPDATE consignments SET status = 'PAID', payment_date = ? WHERE consignment_id = ?";

        List<ProfitSharing> paymentsToUpdate = jdbcTemplate.query(selectQuery, new Object[]{supplierId},
                (rs, rowNum) -> new ProfitSharing(
                        rs.getString("consignment_id"),
                        rs.getString("sale_id"),
                        rs.getString("product_id"),
                        rs.getString("supplier_id"),
                        rs.getInt("product_quantity"),
                        rs.getBigDecimal("total_purchase_price"),
                        rs.getBigDecimal("total_sale_price"),
                        rs.getString("status"),
                        rs.getObject("payment_date", OffsetDateTime.class)
                ));

        for (ProfitSharing payment : paymentsToUpdate) {
            jdbcTemplate.update(updateQuery, LocalDateTime.now(), payment.profitSharingId());
        }
    }

    public List<ProfittDto> getPaidPaymentsSummaryBySupplier(String suppId){
        Map<String, ProfittDto> profitMap = new LinkedHashMap<>();

        jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "SELECT ps.*, p.product_name " +
                            "FROM consignments ps " +
                            "JOIN products p ON ps.product_id = p.product_id " +
                            "WHERE ps.status = 'PAID' AND ps.supplier_id = ?"
            );
            ps.setString(1, suppId);
            return ps;
        }, (rs, rowNum) -> {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy/HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

            final String sharingIdFromDb = rs.getString("consignment_id");
            ProfittDto profittDto = profitMap.get(sharingIdFromDb);
            if (profittDto == null) {
                final String saleId = rs.getString("sale_id");
                final String productId = rs.getString("product_id");
                final String supplierId = rs.getString("supplier_id");
                final int productQuantity = rs.getInt("product_quantity");
                final BigDecimal totalPurchasePrice = rs.getBigDecimal("total_purchase_price");
                final BigDecimal totalSalePrice = rs.getBigDecimal("total_sale_price");
                final String status = rs.getString("status");
                final OffsetDateTime paymentDate = rs.getTimestamp("payment_date") == null ? null
                        : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);
                String formattedDate = formatter.format(paymentDate);
                final String productName = rs.getString("product_name");

                profittDto = new ProfittDto(sharingIdFromDb,saleId, productId, supplierId, productQuantity, totalPurchasePrice, totalSalePrice,status, formattedDate, productName);
                profitMap.put(sharingIdFromDb, profittDto);
            }

            return null;
        });

        return new ArrayList<>(profitMap.values());
    }

    public List<StockProductDto> getStockProductBySupplierId(String supplierId) {
        String sql = "SELECT product_name, stock FROM products WHERE supplier_id = ? AND (deleted_at IS NULL OR deleted_by IS NULL)";

        return jdbcTemplate.query(sql, new Object[]{supplierId}, (rs, rowNum) -> {
            String productName = rs.getString("product_name");
            Integer stock = rs.getInt("stock");

            return new StockProductDto(productName, stock);
        });
    }

    public List<ProfitDto> getProfitSharing() {
        return jdbcTemplate.query(
            "SELECT ps.*, p.product_name FROM consignments ps JOIN products p ON ps.product_id = p.product_id",
            (rs, rowNum) -> {
                OffsetDateTime paymentDate = rs.getTimestamp("payment_date") == null ? null
                    : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);
                return new ProfitDto(
                    rs.getString("consignments_id"),
                    rs.getString("sale_id"),
                    rs.getString("product_id"),
                    rs.getString("supplier_id"),
                    rs.getInt("product_quantity"),
                    rs.getBigDecimal("total_purchase_price"),
                    rs.getString("status"),
                    paymentDate
                );
            }
        );
    }

    public List<ProfitSharing> getProfitSharingByUserId(String userId) {
        return jdbcTemplate.query(
            "SELECT * FROM consignments WHERE supplier_id = ?",
            (rs, rowNum) -> {
                OffsetDateTime paymentDate = rs.getTimestamp("payment_date") == null ? null
                    : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);
                return new ProfitSharing(
                    rs.getString("consignment_id"),
                    rs.getString("sale_id"),
                    rs.getString("product_id"),
                    rs.getString("supplier_id"),
                    rs.getInt("product_quantity"),
                    rs.getBigDecimal("total_purchase_price"),
                    rs.getBigDecimal("total_sale_price"),
                    rs.getString("status"),
                    paymentDate
                );
            },
            userId
        );
    }

    public List<StockProductDto> getStockProducts() {
        return jdbcTemplate.query(
            "SELECT product_name, stock FROM products WHERE (deleted_at IS NULL OR deleted_by IS NULL)",
            (rs, rowNum) -> new StockProductDto(
                rs.getString("product_name"),
                rs.getInt("stock")
            )
        );
    }

}
