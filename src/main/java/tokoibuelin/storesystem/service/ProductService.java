package tokoibuelin.storesystem.service;

import org.springframework.stereotype.Service;
import tokoibuelin.storesystem.entity.Product;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Page;
import tokoibuelin.storesystem.model.request.*;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.response.ProductDto;
import tokoibuelin.storesystem.model.response.allProductDto;
import tokoibuelin.storesystem.repository.ProductRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
@Service
public class ProductService extends AbstractService{
    private final ProductRepository productRepository;

    public ProductService(final ProductRepository productRepository){
        this.productRepository =  productRepository;
    }
    public Response<Object> listProducts(final Authentication authentication, final int page, final int size) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PEMILIK).orElseGet(() -> {
            if (page <= 0 || size <= 0) {
                return Response.badRequest();
            }
            Page<Product> productPage = productRepository.listProducts(page, size);
            List<ProductDto> products = productPage.data().stream().map(product -> new ProductDto(product.productId(), product.productName(),product.price(),product.stock())).toList();
            Page<ProductDto> p = new Page<>(productPage.totalData(), productPage.totalPage(), productPage.page(), productPage.size(), products);
            return Response.create("09", "00", "Sukses", p);
        });
    }


    public Response<Object> allProducts() {
//        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK,User.Role.PELANGGAN).orElseGet(() -> {

            List<Product> products = productRepository.allProducts();
            List<allProductDto> productDtos = products.stream()
                    .map(product -> new allProductDto( product.productId(),product.productName(),product.description(), product.unit(), product.price(), product.stock(), product.supplierId(),product.productImage(),product.purchasePrice()))
                    .toList();
            return Response.create("09", "00", "Sukses", productDtos);
//        });
    }

    public Response<Object> getAllProducts() {
            List<Product> products = productRepository.allProducts();
            List<allProductDto> productDtos = products.stream()
                    .map(product -> new allProductDto( product.productId(),product.productName(),product.description(), product.unit(), product.price(), product.stock(), product.supplierId(),product.productImage(),product.purchasePrice()))
                    .toList();

            return Response.create("09", "00", "Sukses", productDtos);
    }


    public Response<Object> createProduct(final Authentication authentication, final RegistProductReq req) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PEMILIK).orElseGet(() -> {
            if (req == null) {
                return Response.badRequest();
            }

            String supplierId = req.supplierId();
            String supplierIdToSave = (supplierId != null && !supplierId.isEmpty()) ? supplierId : null;

            final Product product = new Product( //
                    null, //
                    req.productName(),//
                    req.description(), //
                    req.unit(),
                    req.price(), //
                    req.stock(),//
                    supplierIdToSave,
                    req.productImage(),
                    req.categoryId(),
                    req.purchasePrice(),
                    authentication.id(),
                    null,
                    null,
                    OffsetDateTime.now(),
                    null,
                    null

                    


            );
            final String saved = productRepository.saveProduct(product);
            if (null == saved) {
                return Response.create("05", "01", "Gagal menambahkan Product", null);
            }
            return Response.create("05", "00", "Sukses", saved);
        });
    }

    public Response<Object> deleteProduct(Authentication authentication, String productId) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PEMILIK).orElseGet(() -> {
            Optional<Product> productOpt = productRepository.findById(productId);

            if (!productOpt.isPresent()) {
                return Response.create("10", "02", "ID tidak ditemukan", null);
            }

            Product product = productOpt.get();

            if (product.deletedAt() != null) {
                return Response.create("10", "03", "Data sudah dihapus", null);
            }

            Product deletedProduct = new Product(
                    product.productId(),
                    product.productName(),
                    product.description(),
                    product.unit(),
                    product.price(),
                    product.stock(),
                    product.supplierId(),
                    product.productImage(),
                    product.categoryId(),
                    product.purchasePrice(),
                    product.createdBy(),
                    product.updatedBy(),
                    authentication.id(),
                    product.createdAt(),
                    product.updatedAt(),
                    OffsetDateTime.now());

            Long updatedRows = productRepository.deleteProduct(deletedProduct);
            if (updatedRows > 0) {
                return Response.create("10", "00", "Berhasil hapus data", null);
            } else {
                return Response.create("10", "01", "Gagal hapus data", null);
            }
        });
    }

    public Response<Object> updateProduct(final Authentication authentication, final UpdateProductReq req) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PEMILIK).orElseGet(() -> {
            Optional<Product> productOpt = productRepository.findById(req.productId());
            if (productOpt.isEmpty()) {
                return Response.create("07", "01", "Produk  tidak ditemukan", null);
            }
            Product product = productOpt.get();
            Product updatedProduct = new Product(
                    product.productId(),
                    req.productName(),
                    product.description(),
                    req.unit(),
                    req.price(),
                    product.stock(),
                    product.supplierId(),
                    product.productImage(),
                    product.categoryId(),
                    product.purchasePrice(),
                    product.createdBy(),
                    authentication.id(),
                    product.deletedBy(),
                    product.createdAt(),
                    OffsetDateTime.now(),
                    product.deletedAt());

            if (productRepository.updateProduct(updatedProduct,authentication.id())) {
                return Response.create("07", "00", "Data produk berhasil diperbarui", null);
            } else {
                return Response.create("07", "02", "Gagal mengupdate data produk", null);
            }
        });
    }

    public Response<Object> addStockProduct(final Authentication authentication, final Long addStock, final String produkId) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PEMILIK).orElseGet(() -> {
            Optional<Product> productOpt = productRepository.findById(produkId);
            if (productOpt.isEmpty()) {
                return Response.create("07", "01", "Produk  tidak ditemukan", null);
            }

            Product product = productOpt.get();
            Long newStock = product.stock() +addStock;
            Product updatedProduct = new Product(
                    product.productId(),
                    product.productName(),
                    product.description(),
                    product.unit(),
                    product.price(),
                    newStock,
                    product.supplierId(),
                    product.productImage(),
                    product.categoryId(),
                    product.purchasePrice(),
                    product.createdBy(),
                    authentication.id(),
                    product.deletedBy(),
                    product.createdAt(),
                    OffsetDateTime.now(),
                    product.deletedAt());

            if (productRepository.updateStockProduct(updatedProduct,newStock)) {
                return Response.create("07", "00", "Data produk berhasil diperbarui", null);
            } else {
                return Response.create("07", "02", "Gagal mengupdate data produk", null);
            }
        });
    }

}
