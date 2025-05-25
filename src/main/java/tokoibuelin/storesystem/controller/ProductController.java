package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.entity.Product;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.RegistProductReq;
import tokoibuelin.storesystem.model.request.UpdateProductReq;
import tokoibuelin.storesystem.model.response.StockDto;
import tokoibuelin.storesystem.repository.ProductRepository;
import tokoibuelin.storesystem.service.ProductService;
import tokoibuelin.storesystem.util.SecurityContextHolder;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/secured/product")
@CrossOrigin(origins = {"http://127.0.0.1:5500","http://127.0.0.1:3000"})
public class ProductController {
    @Autowired
    private  ProductService productService;
    @Autowired
    private ProductRepository productRepository;


    @GetMapping("/all")
    public List<Product> allProducts() {

        return productRepository.getAllProduct();
    }
    @GetMapping("/find/{find}")
    public Optional<Product> findProduct(@PathVariable String find){
        return productRepository.findProduct(find);
    }

    @GetMapping("/getStockProduct")
    public List<StockDto> getStockProduct(){
        return productRepository.getStockProduct();
    }
    @PostMapping("/add-product")
    public Response<Object> createProduct(@RequestBody RegistProductReq req){
        Authentication authentication = SecurityContextHolder.getAuthentication();
        System.out.printf("CEKKKKKK -> ",req);
        return productService.createProduct(authentication,req);
    }
    @PostMapping("/update-product")
    public Response<Object> updateProduct(@RequestBody UpdateProductReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return productService.updateProduct(authentication, req);
    }

    @PostMapping("/add-stock/{addStock}/{productId}")
    public Response<Object> addStockProduct( @PathVariable Long addStock, @PathVariable String productId ) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return productService.addStockProduct(authentication, addStock,productId);
    }

    @DeleteMapping("/delete-product/{productId}")
    public Response<Object> deleteProduct(@PathVariable String productId) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return productService.deleteProduct(authentication, productId);
    }
}
