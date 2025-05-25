package tokoibuelin.storesystem.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

import tokoibuelin.storesystem.entity.Category;
import tokoibuelin.storesystem.entity.Product;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.AddCategoryReq;
import tokoibuelin.storesystem.model.response.CategoryDto;
import tokoibuelin.storesystem.model.response.allProductDto;
import tokoibuelin.storesystem.repository.CategoryRepository;

@Service
public class CategoryService extends AbstractService{
    
    private final CategoryRepository categoryRepository;

    public CategoryService(final CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    
    public Response<Object> allCategories() {
//        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK,User.Role.PELANGGAN).orElseGet(() -> {

            List<Category> categories = categoryRepository.getAllCategory();
            List<CategoryDto> categoryDtos = categories.stream()
                    .map(category -> new CategoryDto( category.categoryId(),category.categoryName()))
                    .toList();

            return Response.create("09", "00", "Sukses", categoryDtos);
//        });
    }


    public Response<Object> addCategory(final Authentication authentication, final AddCategoryReq req) {
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {
            if (req == null) {
                return Response.badRequest();
            }
            final Category category = new Category( 
                    null,
                    req.categoryName()
            );
            final Long saved = categoryRepository.saveCategory(category);
            if (0L == saved) {
                return Response.create("05", "01", "Gagal menambhkan kategori", null);
            }
            return Response.create("05", "00", "Sukses", saved);
        });
    }

    public Response<Object> deleteCategory(Authentication authentication, Integer id) {
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {
            Optional<Category> catOpt = categoryRepository.findById(id);

            if (!catOpt.isPresent()) {
                return Response.create("10", "02", "ID tidak ditemukan", null);
            }

            long updatedRows = categoryRepository.deleteCategory(id);
            if (updatedRows > 0) {
                return Response.create("10", "00", "Berhasil hapus data", null);
            } else {
                return Response.create("10", "01", "Gagal hapus data", null);
            }
        });
    }
}
