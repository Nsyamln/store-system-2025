package tokoibuelin.storesystem.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tokoibuelin.storesystem.entity.Category;

@Repository
public class CategoryRepository {
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
     private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Autowired
    public List<Category> getAllCategory() {
        return jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM " + Category.TABLE_NAME );
            return ps;
        }, (rs, rowNum) -> {
            final Integer categoryId = rs.getInt("id");
            final String categoryName = rs.getString("category_name");
            return new Category(categoryId, categoryName);
        });
    }

    // public long saveCategory(final Category category) {
    //     final KeyHolder keyHolder = new GeneratedKeyHolder();
    //     try {
    //         if (jdbcTemplate.update(con -> Objects.requireNonNull(category.insert(con)), keyHolder) != 1) {
    //             return 0L;
    //         } else {
    //             return Objects.requireNonNull(keyHolder.getKey()).longValue();
    //         }
    //     } catch (Exception e) {
    //         log.error("{}", e.getMessage());
    //         return 0L;
    //     }
    // }

    public long saveCategory(final Category category) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            if (jdbcTemplate.update(con -> Objects.requireNonNull(category.insert(con)), keyHolder) != 1) {
                return 0L;
            } else {
                Map<String, Object> keys = keyHolder.getKeys();
                 if (keys != null && keys.containsKey("id")) { // Atau jika nama kolom ID hanya "id"
                    return ((Number) keys.get("id")).longValue();
                }
                // Handle kasus jika kunci tidak ditemukan (mungkin log error)
                log.error("Gagal mendapatkan ID kategori setelah penyimpanan.");
                return 0L;
            }
        } catch (Exception e) {
            log.error("Error during saveCategory: {}", e.getMessage());
            return 0L;
        }
    }
    public long deleteCategory(Integer id) {
        try {
            String sql = "DELETE FROM " + Category.TABLE_NAME + "  WHERE id=?";
            return jdbcTemplate.update(sql, id);
        } catch (Exception e) {
            log.error("Gagal untuk menghapus kategori: {}", e.getMessage());
            return 0L;
        }
    }





   
    public Optional<Category> findById(Integer id) {
        
        if (id == null ) {
            return Optional.empty();
        }
        return Optional.ofNullable(jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM " + Category.TABLE_NAME + "  WHERE id = ?");
            ps.setInt(1, id);
            return ps;
        }, rs -> {
            if (rs.next()) {
                final Integer categoryId = rs.getInt("id");
                final String categoryName = rs.getString("category_name");
                return new Category(categoryId, categoryName);

            }
            return null;
        }));
    }
}
