package tokoibuelin.storesystem.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tokoibuelin.storesystem.entity.*;
import tokoibuelin.storesystem.model.response.SaleDto;
import tokoibuelin.storesystem.model.response.SalesDto;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class SaleRepository {
    private static final Logger log = LoggerFactory.getLogger(SaleRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public SaleRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SalesDto> getSalesWithDetails() {
        // Fetch all rows from the database
        List<SalesDto> saleDtos = jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "SELECT s.*, sd.* FROM sales s LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id WHERE (customer_id IS NULL OR order_id IS  NULL)"
            );
            return ps;
        }, (rs, rowNum) -> {
            final String saleId = rs.getString("sale_id");

            // Ambil sale_date sebagai Timestamp atau OffsetDateTime
            OffsetDateTime saleDateTime = rs.getObject("sale_date", OffsetDateTime.class);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
            String saleDate = formatter.format(saleDateTime);
            final BigDecimal totalPrice = rs.getBigDecimal("total_price");
            final String customerId = rs.getString("customer_id");
            final String orderId = rs.getString("order_id");
            final BigDecimal amountPaid = rs.getBigDecimal("amount_paid");
            final String paymentMethod = rs.getString("payment_method");

            // Extract SaleDetails data
            final String detailId = rs.getString("detail_id");
            final String productId = rs.getString("product_id");
            final String productName = rs.getString("product_name");
            final int quantity = rs.getInt("quantity");
            final BigDecimal price = rs.getBigDecimal("price");

            // Create SaleDetails object
            SaleDetails saleDetails = new SaleDetails(detailId, saleId, productId, productName, quantity, price);

            // Return partial SaleDto with a list containing a single SaleDetails
            return new SalesDto(saleId, saleDate, totalPrice, customerId, orderId, amountPaid, paymentMethod, List.of(saleDetails));
        });

        // Group SaleDetails by saleId
        return saleDtos.stream()
                .collect(Collectors.toMap(
                        SalesDto::saleId, // Key by saleId
                        salesDto -> new SalesDto(
                                salesDto.saleId(),
                                salesDto.saleDate(),
                                salesDto.totalPrice(),
                                salesDto.customerId(),
                                salesDto.orderId(),
                                salesDto.amountPaid(),
                                salesDto.paymentMethod(),
                                new ArrayList<>() // Initialize empty list for SaleDetails
                        ),
                        (existing, replacement) -> {
                            existing.saleDetails().addAll(replacement.saleDetails()); // Merge SaleDetails lists
                            return existing;
                        }
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    public List<SaleDto> getSalesById(String saleId) {
        Map<String, SaleDto> saleMap = new LinkedHashMap<>();

        jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "SELECT s.*, sd.*, u.name FROM sales s LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id LEFT JOIN users u ON s.customer_id = u.user_id WHERE s.sale_id = ?"
            );
            ps.setString(1, saleId);
            return ps;
        }, (rs, rowNum) -> {
            final String saleIdFromDb = rs.getString("sale_id");
            SaleDto saleDto = saleMap.get(saleIdFromDb);
            if (saleDto == null) {
                final OffsetDateTime saleDate = rs.getTimestamp("sale_date") == null ? null
                        : rs.getTimestamp("sale_date").toInstant().atOffset(ZoneOffset.UTC);
                final BigDecimal totalPrice = rs.getBigDecimal("total_price");
                final String customerId = rs.getString("customer_id");
                final String customerName = rs.getString("name");
                final String orderId = rs.getString("order_id");
                final BigDecimal amountPaid = rs.getBigDecimal("amount_paid");
                final String paymentMethod = rs.getString("payment_method");

                saleDto = new SaleDto(saleIdFromDb, saleDate, totalPrice, customerId,customerName, orderId, amountPaid, paymentMethod, new ArrayList<>());
                saleMap.put(saleIdFromDb, saleDto);
            }

            final String detailId = rs.getString("detail_id");
            if (detailId != null) {
                final String productId = rs.getString("product_id");
                final String productName = rs.getString("product_name");
                final int quantity = rs.getInt("quantity");
                final BigDecimal price = rs.getBigDecimal("price");

                SaleDetails saleDetails = new SaleDetails(detailId, saleIdFromDb, productId, productName, quantity, price);
                saleDto.saleDetails().add(saleDetails);
            }

            return null;
        });

        return new ArrayList<>(saleMap.values());
    }

    public String saveSale(final Sale sale) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            int updateCount = jdbcTemplate.update(con -> {
                PreparedStatement ps = sale.insert(con);
                System.out.println("Cek save Sale -> "+ps.toString());
                return ps;
            }, keyHolder);
            if (updateCount == 1) {
                System.out.println("Insert successful, keyHolder keys: " + keyHolder.getKeys());
            } else {
                System.out.println("Insert failed, no rows affected.");
            }

            if (updateCount != 1) {
                log.warn("Update count was not 1, it was: {}", updateCount);
                return null;
            }

            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && keys.containsKey("sale_id")) {
                return (String) keys.get("sale_id");
            }

            return null;
        } catch (Exception e) {
            log.error("Error during saveSale: {}", e.getMessage());
            return null;
        }
    }
    public List<SaleDto> getSalesReportAll(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, formatter);

        Timestamp startTimestamp = Timestamp.valueOf(startLocalDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endLocalDate.plusDays(1).atStartOfDay()); // endDate exclusive

        System.out.println("Cek startTimestamp : " + startTimestamp);
        System.out.println("Cek endTimestamp   : " + endTimestamp);

        Map<String, SaleDto> saleMap = new LinkedHashMap<>();

        return jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "SELECT s.*, sd.*, u.name " +
                            "FROM sales s " +
                            "LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id " +
                            "LEFT JOIN users u ON s.customer_id = u.user_id " +
                            "WHERE s.sale_date BETWEEN ? AND ? " +
                            "ORDER BY CAST(SUBSTRING(s.sale_id FROM '([0-9]+)') AS INTEGER) DESC"
            );
            ps.setTimestamp(1, startTimestamp);
            ps.setTimestamp(2, endTimestamp);

            System.out.println("Executing query with parameters: ");
            System.out.println("Start Date: " + startTimestamp);
            System.out.println("End Date: " + endTimestamp);

            return ps;
        }, (rs, rowNum) -> {
            final String saleIdFromDb = rs.getString("sale_id");
            SaleDto saleDto = saleMap.get(saleIdFromDb);
            if (saleDto == null) {
                LocalDateTime localDateTime = rs.getTimestamp("sale_date").toLocalDateTime();
                OffsetDateTime saleDate = localDateTime.atOffset(ZoneOffset.UTC);
                final BigDecimal totalPrice = rs.getBigDecimal("total_price");
                final String customerId = rs.getString("customer_id");
                final String customerName = rs.getString("name");
                final String orderId = rs.getString("order_id");
                final BigDecimal amountPaid = rs.getBigDecimal("amount_paid");
                final String paymentMethod = rs.getString("payment_method");
                saleDto = new SaleDto(saleIdFromDb, saleDate, totalPrice, customerId, customerName, orderId, amountPaid, paymentMethod, new ArrayList<>());
                saleMap.put(saleIdFromDb, saleDto);
            }

            final String detailId = rs.getString("detail_id");
            if (detailId != null) {
                final String productId = rs.getString("product_id");
                final String productName = rs.getString("product_name");
                final int quantity = rs.getInt("quantity");
                final BigDecimal price = rs.getBigDecimal("price");

                SaleDetails saleDetails = new SaleDetails(detailId, saleIdFromDb, productId, productName, quantity, price);
                saleDto.saleDetails().add(saleDetails);
            }

            return null;
        });
    }


    public List<SaleDto> getSalesReportByPaymentMethodAll(final String startDate, final String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, formatter);

        Timestamp startTimestamp = Timestamp.valueOf(startLocalDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endLocalDate.plusDays(1).atStartOfDay()); // endDate exclusive

        return jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "SELECT s.*, sd.*, u.name " +
                            "FROM sales s " +
                            "LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id " +
                            "LEFT JOIN users u ON s.customer_id = u.user_id " +
                            "WHERE s.sale_date BETWEEN ? AND ? " +
                            "ORDER BY CAST(SUBSTRING(s.sale_id FROM '([0-9]+)') AS INTEGER) DESC"
            );
            ps.setTimestamp(1, startTimestamp);
            ps.setTimestamp(2, endTimestamp);

            return ps;
        }, new ResultSetExtractor<List<SaleDto>>() {
            @Override
            public List<SaleDto> extractData(ResultSet rs) throws SQLException, DataAccessException {
                Map<String, SaleDto> saleMap = new LinkedHashMap<>();

                while (rs.next()) {
                    final String saleIdFromDb = rs.getString("sale_id");
                    SaleDto saleDto = saleMap.get(saleIdFromDb);
                    if (saleDto == null) {
                        final OffsetDateTime saleDate = rs.getTimestamp("sale_date") == null ? null
                                : rs.getTimestamp("sale_date").toInstant().atOffset(ZoneOffset.UTC);
                        final BigDecimal totalPrice = rs.getBigDecimal("total_price") != null ? rs.getBigDecimal("total_price") : BigDecimal.ZERO;
                        final String customerId = rs.getString("customer_id") != null ? rs.getString("customer_id") : "";
                        final String customerName = rs.getString("name") != null ? rs.getString("name") : "";
                        final String orderId = rs.getString("order_id") != null ? rs.getString("order_id") : "";
                        final BigDecimal amountPaid = rs.getBigDecimal("amount_paid") != null ? rs.getBigDecimal("amount_paid") : BigDecimal.ZERO;
                        final String paymentMethodStr = rs.getString("payment_method");
//                        String paymentMethodDb = null;
//                        if (paymentMethodStr != null) {
//                            try {
//                                paymentMethodDb = Sale.PaymentMethod.valueOf(paymentMethodStr);
//                            } catch (IllegalArgumentException e) {
//                                // Handle invalid payment method if necessary
//                                System.err.println("Invalid payment method: " + paymentMethodStr);
//                            }
//                        }

                        saleDto = new SaleDto(saleIdFromDb, saleDate, totalPrice, customerId,customerName, orderId, amountPaid, paymentMethodStr, new ArrayList<>());
                        saleMap.put(saleIdFromDb, saleDto);
                    }

                    final String detailId = rs.getString("detail_id");
                    if (detailId != null) {
                        final String productId = rs.getString("product_id");
                        final String productName = rs.getString("product_name");
                        final int quantity = rs.getInt("quantity");
                        final BigDecimal price = rs.getBigDecimal("price");

                        SaleDetails saleDetails = new SaleDetails(detailId, saleIdFromDb, productId, productName, quantity, price);
                        saleDto.getSaleDetails().add(saleDetails);
                    }
                }

                return new ArrayList<>(saleMap.values());
            }
        });
    }

    public List<SaleDto> getSalesReportByPaymentMethod(final String paymentMethod, final String startDate, final String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, formatter);

        Timestamp startTimestamp = Timestamp.valueOf(startLocalDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endLocalDate.plusDays(1).atStartOfDay()); // endDate exclusive

        System.out.println("Cek startTimestamp : " + startTimestamp);
        System.out.println("Cek endTimestamp   : " + endTimestamp);

        Map<String, SaleDto> saleMap = new LinkedHashMap<>();

        jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "SELECT s.*, sd.*, u.name " +
                            "FROM sales s " +
                            "LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id " +
                            "LEFT JOIN users u ON s.customer_id = u.user_id " +
                            "WHERE s.payment_method = ? AND s.sale_date BETWEEN ? AND ? " +
                            "ORDER BY CAST(SUBSTRING(s.sale_id FROM '([0-9]+)') AS INTEGER) DESC"
            );
            ps.setString(1, paymentMethod);
            ps.setTimestamp(2, startTimestamp);
            ps.setTimestamp(3, endTimestamp);

            System.out.println("Executing query with parameters: ");
            System.out.println("Payment Method: " + paymentMethod);
            System.out.println("Start Date: " + startTimestamp);
            System.out.println("End Date: " + endTimestamp);

            return ps;
        }, (rs, rowNum) -> {
            final String saleIdFromDb = rs.getString("sale_id");
            SaleDto saleDto = saleMap.get(saleIdFromDb);
            if (saleDto == null) {
                final OffsetDateTime saleDate = rs.getTimestamp("sale_date") == null ? null
                        : rs.getTimestamp("sale_date").toInstant().atOffset(ZoneOffset.UTC);
                final BigDecimal totalPrice = rs.getBigDecimal("total_price");
                final String customerId = rs.getString("customer_id");
                final String customerName = rs.getString("name");
                final String orderId = rs.getString("order_id");
                final BigDecimal amountPaid = rs.getBigDecimal("amount_paid");
                final String paymentMethodDb = rs.getString("payment_method");

                saleDto = new SaleDto(saleIdFromDb, saleDate, totalPrice, customerId,customerName, orderId, amountPaid, paymentMethodDb, new ArrayList<>());
                saleMap.put(saleIdFromDb, saleDto);
            }

            final String detailId = rs.getString("detail_sale_id");
            if (detailId != null) {
                final String productId = rs.getString("product_id");
                final String productName = rs.getString("product_name");
                final int quantity = rs.getInt("quantity");
                final BigDecimal price = rs.getBigDecimal("price");

                SaleDetails saleDetails = new SaleDetails(detailId, saleIdFromDb, productId, productName, quantity, price);
                saleDto.saleDetails().add(saleDetails);
            }

            return null;
        });

        return new ArrayList<>(saleMap.values());
    }

    public long saveSaleDetails(List<SaleDetails> saleDetails) {
        String sqlDetail = "INSERT INTO sale_details (sale_id, product_id, product_name, quantity, price) VALUES (?, ?, ?, ?, ?)";

        try {
            for (SaleDetails detail : saleDetails) {
                jdbcTemplate.update(sqlDetail, detail.saleId(), detail.productId(), detail.productName(), detail.quantity(), detail.price());
            }
            return 1L;
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    public Long sumSales(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, formatter);

        Timestamp startTimestamp = Timestamp.valueOf(startLocalDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endLocalDate.atTime(LocalTime.MAX));
        try {
            String sql = "SELECT SUM(total_price) AS totalPenjualan FROM sales WHERE sale_date BETWEEN ? AND ?";
            Long totalPenjualan = jdbcTemplate.queryForObject(sql, new Object[]{startTimestamp, endTimestamp}, Long.class);
            System.out.println("totalPenjualan : " + totalPenjualan);
            return totalPenjualan != null ? totalPenjualan : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public Long sumByPaymentMethod(String startDate, String endDate, String paymentMethod) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, formatter);

        // Set start timestamp to the start of the day
        Timestamp startTimestamp = Timestamp.valueOf(startLocalDate.atStartOfDay());
        // Set end timestamp to the end of the day (23:59:59)
        Timestamp endTimestamp = Timestamp.valueOf(endLocalDate.atTime(LocalTime.MAX));

        System.out.println("Start Timestamp: " + startTimestamp);
        System.out.println("End Timestamp: " + endTimestamp);

        try {
            String sql = "SELECT SUM(total_price) AS jumlah FROM sales WHERE sale_date BETWEEN ? AND ? AND payment_method = ?";
            Long jumlah = jdbcTemplate.queryForObject(sql, new Object[]{startTimestamp, endTimestamp, paymentMethod}, Long.class);
            System.out.println("sum "+paymentMethod+" : " + jumlah);
            return jumlah != null ? jumlah : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }





//    public Optional<OrderDto> findByOrderId(String  id) {
//        System.out.println("ID nya : " + id);
//        if (id == null || id == "") {
//            return Optional.empty();
//        }
//        return Optional.ofNullable(jdbcTemplate.query(con -> {
//            final PreparedStatement ps = con.prepareStatement("SELECT o.*, s.*, sd.* " +
//                    "FROM orders o " +
//                    "JOIN sales s ON o.order_id = s.order_id " +
//                    "LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id " +
//                    "WHERE o.order_id = ?");
//            ps.setString(1, id);
//            return ps;
//        }, rs -> {
//            if(rs.next()) {
//                //final String orderId = rs.getString("order_id");
//                final OffsetDateTime formattedSaleDate = rs.getTimestamp("sale_date") == null ? null
//                        : rs.getTimestamp("order_date").toInstant().atOffset(ZoneOffset.UTC);
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//                String orderDate = (formattedSaleDate != null) ? formattedSaleDate.format(formatter) : null;
//
//                final String customerId = rs.getString("customer_id");
//                final String deliveryAddress = rs.getString("delivery_address");
//                final Order.Status statuss = Order.Status.valueOf(rs.getString("status"));
//                final String createdBy = rs.getString("created_by");
//                final String updatedBy = rs.getString("updated_by");
//                final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
//                final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
//                final Integer shippingCost = rs.getInt("shipping_cost");
//                final String trackingNumber = rs.getString("tracking_number");
//                final String courier = rs.getString("courier");
//                final String shippingMethod = rs.getString("shipping_method");
//                final OffsetDateTime estimatedDeliveryDate = rs.getTimestamp("estimated_delivery_date") == null ? null : rs.getTimestamp("estimated_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
//                final OffsetDateTime actualDeliveryDate = rs.getTimestamp("actual_delivery_date") == null ? null : rs.getTimestamp("actual_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
//
//                final String saleId = rs.getString("sale_id");
//                final OffsetDateTime saleDate = rs.getTimestamp("sale_date") == null ? null : rs.getTimestamp("sale_date").toInstant().atOffset(ZoneOffset.UTC);
//                final BigDecimal totalPrice = rs.getBigDecimal("total_price");
//                final BigDecimal amountPaid = rs.getBigDecimal("amount_paid");
//                final Sale.PaymentMethod paymentMethod = Sale.PaymentMethod.valueOf(rs.getString("payment_method"));
//
//                // Mendapatkan data dari tabel sale_details
//                final String detailId = rs.getString("detail_id");
//                final String productId = rs.getString("product_id");
//                final String productName = rs.getString("product_name");
//                final Integer quantity = rs.getInt("quantity");
//                final Integer price = rs.getInt("price");
//
//                SaleDetails saleDetails = new SaleDetails(detailId, saleId, productId, productName, quantity, price);
//                Sale sale = new Sale(saleId, saleDate, totalPrice, customerId, id, amountPaid, paymentMethod);
//                return new OrderDto(id, orderDate, customerId, deliveryAddress, statuss, createdBy, updatedBy, createdAt, updatedAt, shippingCost, trackingNumber, courier, shippingMethod, estimatedDeliveryDate, actualDeliveryDate, sale, saleDetails);
//            }
//            return null;
//        }));
//    }



}
