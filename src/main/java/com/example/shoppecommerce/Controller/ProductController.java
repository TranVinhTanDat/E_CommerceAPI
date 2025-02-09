package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.ApiResponse;
import com.example.shoppecommerce.Entity.Product;
import com.example.shoppecommerce.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/page")
    public Page<Product> getAllProductsPagew(@RequestParam int page, @RequestParam int size, @RequestParam(required = false) String category) {
        return productService.getAllProductsPage(page, size, category);
    }

    @GetMapping("/search")
    public Page<Product> searchProducts(@RequestParam int page, @RequestParam int size, @RequestParam String keyword, @RequestParam double maxPrice, @RequestParam(required = false) String category) {
        return productService.searchProducts(page, size, keyword, maxPrice, category);
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/page/admin")
    public Page<Product> getAllProductsPageAdmin(@RequestParam int page, @RequestParam int size, @RequestParam(required = false) String category) {
        return productService.getAllProductsPageAdmin(page, category);
    }

}
