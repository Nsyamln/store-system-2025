package tokoibuelin.storesystem.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tokoibuelin.storesystem.entity.Category;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.AddCategoryReq;
import tokoibuelin.storesystem.repository.CategoryRepository;
import tokoibuelin.storesystem.service.CategoryService;
import tokoibuelin.storesystem.util.SecurityContextHolder;

@RestController
@RequestMapping("/secured/category")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:3000"})
public class CategoryController {
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    CategoryService categoryService;

    @GetMapping("/all-categories")
    public ResponseEntity<Response<Object>> getAllCategories() {
        Response<Object> response = categoryService.allCategories();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-category")
    public Response<Object> addCategory(@RequestBody AddCategoryReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return categoryService.addCategory(authentication, req);
    }

    @DeleteMapping("/delete-category/{id}")
    public Response<Object> deleteCategory(@PathVariable Integer id){
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return categoryService.deleteCategory(authentication, id);
    }

}
