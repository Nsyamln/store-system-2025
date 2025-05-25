package tokoibuelin.storesystem.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tokoibuelin.storesystem.entity.*;
import tokoibuelin.storesystem.model.response.OrderDto;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class OrderRepository {
    private static final Logger log = LoggerFactory.getLogger(OrderRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public String getLatestOrderId() {
        String sql = "SELECT MAX(order_id) FROM orders";
        return jdbcTemplate.queryForObject(sql, String.class);
    }
    public String generateNewOrderId() {
        String latestOrderId = getLatestOrderId();
        if (latestOrderId == null) {
            // Jika tidak ada order_id di database, mulai dari OR001
            return "OR001";
        }

        // Ambil bagian numerik dari order_id
        int latestNumber = Integer.parseInt(latestOrderId.substring(2));

        // Buat order_id baru dengan menambahkan 1
        int newNumber = latestNumber + 1;
        return String.format("OR%03d", newNumber);
    }
    public String saveOrder(final Order order) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            int updateCount = jdbcTemplate.update(con -> {
                PreparedStatement ps = order.insert(con);
                System.out.println("Cek Save Order : "+ps.toString());
                return ps;
            }, keyHolder);

            if (updateCount != 1) {
                return null; // atau return 0L sesuai kebutuhan
            }

            // Ambil hasil dari KeyHolder sebagai String
            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && keys.containsKey("order_id")) {
                return (String) keys.get("order_id");
            }

            return null;
        } catch (Exception e) {
            log.error("Error during saveOrder: {}", e.getMessage());
            return null; 
        }
    }
        
    public long saveOrderDetails(List<OrderDetails> orderDetails) {
        String sqlDetail = "INSERT INTO order_details (order_id, product_id, product_name, quantity, price, unit) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            for (OrderDetails detail : orderDetails) {
                jdbcTemplate.update(sqlDetail, detail.orderId(), detail.productId(), detail.productName(), detail.quantity(), detail.price(), detail.unit());
            }
            return 1L;
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    public Optional<Order> findById(String  id) {
        System.out.println("ID nya : " + id);
        if (id == null || id == "") {
            return Optional.empty();
        }
        return Optional.ofNullable(jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM " + Order.TABLE_NAME + " WHERE order_id=?");
            ps.setString(1, id);
            return ps;
        }, rs -> {
            if(rs.next()) {
                final OffsetDateTime orderDate = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("order_date").toInstant().atOffset(ZoneOffset.UTC);
                final String customerId = rs.getString("customer_id");
                final String deliveryAddress = rs.getString("delivery_address");
                final String phone = rs.getString("phone");
                final Order.Status status = Order.Status.valueOf(rs.getString("status"));
                final String createdBy = rs.getString("created_by");
                final String updatedBy = rs.getString("updated_by");
                final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
                final Integer shippingChost = rs.getInt("shipping_cost");
                final String trackingNumber = rs.getString("tracking_number");
                final String courier = rs.getString("courier");
                // final String estimatedDelivery = rs.getString("estimated_delivery");
                final String shippingMethod  =rs.getString("shipping_method");
                final OffsetDateTime estimatedDeliveryDate = rs.getTimestamp("estimated_delivery_date") == null ? null : rs.getTimestamp("estimated_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime actualDeliveryDate = rs.getTimestamp("actual_delivery_date") == null ? null : rs.getTimestamp("actual_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
                return new Order(id, orderDate, customerId, deliveryAddress,phone, status, createdBy, updatedBy, createdAt, updatedAt,shippingChost,trackingNumber,courier,shippingMethod,estimatedDeliveryDate,actualDeliveryDate);
            }
            return null;
        }));
    }

    // public List<OrderDto> getByStatus(String status,int page,int limit) {
    //     System.out.println("Status nya : " + status);
    //     if (status == null || status.isEmpty()) {
    //         return Collections.emptyList(); // Kembalikan list kosong jika status null atau kosong
    //     }

    //     int offset = (page - 1) * limit;

    //     String sql = "SELECT o.*, s.*, sd.*, u.name AS customer_name FROM orders o JOIN sales s ON o.order_id = s.order_id LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id" +
    //             " JOIN users u ON o.customer_id = u.user_id WHERE o.status = ? LIMIT ? OFFSET ?";

    //     return jdbcTemplate.query(sql, new Object[]{status,limit,offset}, (rs, rowNum) -> {
    //         final String id = rs.getString("order_id");
    //         final OffsetDateTime formattedSaleDate = rs.getTimestamp("sale_date") == null ? null
    //                 : rs.getTimestamp("order_date").toInstant().atOffset(ZoneOffset.UTC);
    //         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //         String orderDate = (formattedSaleDate != null) ? formattedSaleDate.format(formatter) : null;

    //         final String customerId = rs.getString("customer_id");
    //         final String customerName = rs.getString("customer_name");
    //         final String deliveryAddress = rs.getString("delivery_address");
    //         final String phone = rs.getString("phone");
    //         final Order.Status statuss = Order.Status.valueOf(rs.getString("status"));
    //         final String createdBy = rs.getString("created_by");
    //         final String updatedBy = rs.getString("updated_by");
    //         final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
    //         final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
    //         final Integer shippingCost = rs.getInt("shipping_cost");
    //         final String trackingNumber = rs.getString("tracking_number");
    //         final String courier = rs.getString("courier");
    //         final String shippingMethod = rs.getString("shipping_method");
    //         final OffsetDateTime estimatedDeliveryDate = rs.getTimestamp("estimated_delivery_date") == null ? null : rs.getTimestamp("estimated_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
    //         final OffsetDateTime actualDeliveryDate = rs.getTimestamp("actual_delivery_date") == null ? null : rs.getTimestamp("actual_delivery_date").toInstant().atOffset(ZoneOffset.UTC);

    //         final String saleId = rs.getString("sale_id");
    //         final OffsetDateTime saleDate = rs.getTimestamp("sale_date") == null ? null : rs.getTimestamp("sale_date").toInstant().atOffset(ZoneOffset.UTC);
    //         final BigDecimal totalPrice = rs.getBigDecimal("total_price");
    //         final BigDecimal amountPaid = rs.getBigDecimal("amount_paid");
    //         final String paymentMethod = rs.getString("payment_method");

    //         // Mendapatkan data dari tabel sale_details
    //         final String detailId = rs.getString("detail_id");
    //         final String productId = rs.getString("product_id");
    //         final String productName = rs.getString("product_name");
    //         final Integer quantity = rs.getInt("quantity");
    //         final BigDecimal price = rs.getBigDecimal("price");
    //         final Integer unit = rs.getInt("unit");

    //         SaleDetails saleDetails = new SaleDetails(detailId, saleId, productId, productName, quantity, price, unit);
    //         Sale sale = new Sale(saleId, saleDate, totalPrice, amountPaid, paymentMethod);
    //         return new OrderDto(id, orderDate, customerId,customerName, deliveryAddress,phone, statuss, createdBy, updatedBy, createdAt, updatedAt, shippingCost, trackingNumber, courier, shippingMethod, estimatedDeliveryDate, actualDeliveryDate, orderDetails);
    //     });
    // }

    // public List<OrderDto> getAll() {

    //     String sql = "SELECT o.*, s.*, sd.*, u.name AS customer_name FROM orders o JOIN sales s ON o.order_id = s.order_id LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id JOIN users u ON o.customer_id = u.user_id";

    //     return jdbcTemplate.query(sql, (rs, rowNum) -> {
    //         final String id = rs.getString("order_id");
    //         final OffsetDateTime formattedSaleDate = rs.getTimestamp("sale_date") == null ? null
    //                 : rs.getTimestamp("order_date").toInstant().atOffset(ZoneOffset.UTC);
    //         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //         String orderDate = (formattedSaleDate != null) ? formattedSaleDate.format(formatter) : null;

    //         final String customerId = rs.getString("customer_id");
    //         final String customerName = rs.getString("customer_name");  
    //         final String deliveryAddress = rs.getString("delivery_address");
    //         final String phone = rs.getString("phone");
    //         final Order.Status statuss = Order.Status.valueOf(rs.getString("status"));
    //         final String createdBy = rs.getString("created_by");
    //         final String updatedBy = rs.getString("updated_by");
    //         final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
    //         final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
    //         final Integer shippingCost = rs.getInt("shipping_cost");
    //         final String trackingNumber = rs.getString("tracking_number");
    //         final String courier = rs.getString("courier");
    //         final String shippingMethod = rs.getString("shipping_method");
    //         final OffsetDateTime estimatedDeliveryDate = rs.getTimestamp("estimated_delivery_date") == null ? null : rs.getTimestamp("estimated_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
    //         final OffsetDateTime actualDeliveryDate = rs.getTimestamp("actual_delivery_date") == null ? null : rs.getTimestamp("actual_delivery_date").toInstant().atOffset(ZoneOffset.UTC);

    //         final String saleId = rs.getString("sale_id");
    //         final OffsetDateTime saleDate = rs.getTimestamp("sale_date") == null ? null : rs.getTimestamp("sale_date").toInstant().atOffset(ZoneOffset.UTC);
    //         final BigDecimal totalPrice = rs.getBigDecimal("total_price");
    //         final BigDecimal amountPaid = rs.getBigDecimal("amount_paid");
    //         final String paymentMethod = rs.getString("payment_method");

    //         // Mendapatkan data dari tabel sale_details
    //         final String detailId = rs.getString("detail_id");
    //         final String productId = rs.getString("product_id");
    //         final String productName = rs.getString("product_name");
    //         final Integer quantity = rs.getInt("quantity");
    //         final BigDecimal price = rs.getBigDecimal("price");
    //         final Integer unit = rs.getInt("unit");

    //         SaleDetails saleDetails = new SaleDetails(detailId, saleId, productId, productName, quantity, price, unit);
    //         Sale sale = new Sale(saleId, saleDate, totalPrice,  amountPaid, paymentMethod);
    //         return new OrderDto(id, orderDate, customerId, customerName, deliveryAddress, phone,statuss, createdBy, updatedBy, createdAt, updatedAt, shippingCost, trackingNumber, courier, shippingMethod, estimatedDeliveryDate, actualDeliveryDate, sale, saleDetails);
    //     });
    // }


    // public Optional<OrderDto> findByOrderId(String  id) {
    //     System.out.println("ID nya : " + id);
    //     if (id == null || id == "") {
    //         return Optional.empty();
    //     }
    //     return Optional.ofNullable(jdbcTemplate.query(con -> {
    //         final PreparedStatement ps = con.prepareStatement("SELECT o.*, s.*, sd.*, u.name AS customer_name" +
    //                 " FROM orders o JOIN sales s ON o.order_id = s.order_id LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id" +
    //                 " JOIN users u ON o.customer_id = u.user_id WHERE o.order_id = ?");
    //         ps.setString(1, id);
    //         return ps;
    //     }, rs -> {
    //         if(rs.next()) {
    //             //final String orderId = rs.getString("order_id");
    //             final OffsetDateTime formattedSaleDate = rs.getTimestamp("sale_date") == null ? null
    //                     : rs.getTimestamp("order_date").toInstant().atOffset(ZoneOffset.UTC);
    //             DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //             String orderDate = (formattedSaleDate != null) ? formattedSaleDate.format(formatter) : null;

    //             final String customerId = rs.getString("customer_id");
    //             final String customerName = rs.getString("customer_name");  // Nama user dari tabel users
    //             final String deliveryAddress = rs.getString("delivery_address");
    //             final String phone = rs.getString("phone");
    //             final Order.Status statuss = Order.Status.valueOf(rs.getString("status"));
    //             final String createdBy = rs.getString("created_by");
    //             final String updatedBy = rs.getString("updated_by");
    //             final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
    //             final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
    //             final Integer shippingCost = rs.getInt("shipping_cost");
    //             final String trackingNumber = rs.getString("tracking_number");
    //             final String courier = rs.getString("courier");
    //             final String shippingMethod = rs.getString("shipping_method");
    //             final OffsetDateTime estimatedDeliveryDate = rs.getTimestamp("estimated_delivery_date") == null ? null : rs.getTimestamp("estimated_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
    //             final OffsetDateTime actualDeliveryDate = rs.getTimestamp("actual_delivery_date") == null ? null : rs.getTimestamp("actual_delivery_date").toInstant().atOffset(ZoneOffset.UTC);

    //             final String saleId = rs.getString("sale_id");
    //             final OffsetDateTime saleDate = rs.getTimestamp("sale_date") == null ? null : rs.getTimestamp("sale_date").toInstant().atOffset(ZoneOffset.UTC);
    //             final BigDecimal totalPrice = rs.getBigDecimal("total_price");
    //             final BigDecimal amountPaid = rs.getBigDecimal("amount_paid");
    //             final String paymentMethod = rs.getString("payment_method");

    //             // Mendapatkan data dari tabel sale_details
    //             final String detailId = rs.getString("detail_id");
    //             final String productId = rs.getString("product_id");
    //             final String productName = rs.getString("product_name");
    //             final Integer quantity = rs.getInt("quantity");
    //             final BigDecimal price = rs.getBigDecimal("price");
    //             final Integer unit = rs.getInt("unit");

    //             SaleDetails saleDetails = new SaleDetails(detailId, saleId, productId, productName, quantity, price, unit);
    //             Sale sale = new Sale(saleId, saleDate, totalPrice,amountPaid, paymentMethod);
    //             return new OrderDto(id, orderDate, customerId,customerName, deliveryAddress,phone, statuss, createdBy, updatedBy, createdAt, updatedAt, shippingCost, trackingNumber, courier, shippingMethod, estimatedDeliveryDate, actualDeliveryDate, sale, saleDetails);
    //         }
    //         return null;
    //     }));
    // }
    public Long updateOrderStatus(String authId, String id, Order.Status status) {
        try {
            return (long) jdbcTemplate.update(con -> {
                final PreparedStatement ps = con.prepareStatement(
                        "UPDATE " + Order.TABLE_NAME + " SET status = ?,updated_by=?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?");
                ps.setString(1, status.toString());
                ps.setObject(2, authId);
                ps.setString(3, id);
                return ps;
            });
        } catch (Exception e) {
            log.error("Gagal update status Auction: {}", e.getMessage());
            return 0L;
        }
    }


    public boolean addResi(final Order order, final String userId) {
        final String sql = "UPDATE " + Order.TABLE_NAME + " SET tracking_number=?,updated_by=?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, order.trackingNumber());
                ps.setString(2, userId);
                ps.setString(3, order.orderId());
                return ps;
            });
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("Gagal untuk update produk: {}", e.getMessage());
            return false;
        }
    }

    public boolean delivered(final Order order, final String userId) {
        final String sql = "UPDATE " + Order.TABLE_NAME + " SET actual_delivery_date = CURRENT_TIMESTAMP ,updated_by=?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, userId);
                ps.setString(2, order.orderId());
                return ps;
            });
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("Gagal untuk update produk: {}", e.getMessage());
            return false;
        }
    }

    public String formatAddress(String addressId) {
        String sql = "SELECT street, rt, rw, village, district, city, postal_code FROM addresses WHERE address_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{addressId}, (rs, rowNum) -> {
            String street = rs.getString("street");
            String rt = rs.getString("rt");
            String rw = rs.getString("rw");
            String village = rs.getString("village");
            String district = rs.getString("district");
            String city = rs.getString("city");
            String postalCode = rs.getString("postal_code");
            return String.format("%s, RT %s/RW %s, %s, %s, %s, %s", street, rt, rw, village, district, city, postalCode);
        });
    }
}

