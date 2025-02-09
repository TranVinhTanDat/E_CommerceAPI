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
@RequestMapping("/admin/productexample")
@PreAuthorize("hasRole('ADMIN')")

public class AdminProductExampleController {
    @Autowired
    ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProduct() {
        List<Product> productList = productService.getAllProducts();
        return new ResponseEntity<>(new ApiResponse<>(true,"List Product Loaded!",productList),HttpStatus.OK);
    }

    @PostMapping("/add-product")
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody Product product) {
        try {
            Product newProduct = productService.saveProduct(product);
            return new ResponseEntity<>(new ApiResponse<>(true,"Add product suscessfully!",newProduct),HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(false,e.getMessage(),null),HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/edit-product/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable long id, @RequestBody Product product) {
        Product idProduct = productService.getProductById(id);
        if (idProduct != null) {
            product.setId(id);
            productService.saveProduct(product);
            return new ResponseEntity<>(new ApiResponse<>(true,"Update product sucessfully!",product),HttpStatus.OK);
        }else {
            return new ResponseEntity<>(new ApiResponse<>(false,"Update product failed!",null),HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete-product")
    public ResponseEntity<ApiResponse<Product>> deleteProduct(@RequestParam long id) {
        Product idProduct = productService.getProductById(id);
        if (idProduct != null) {
            productService.deleteProduct(id);
            return new ResponseEntity<>(new ApiResponse<>(true,"Delete product sucessfully!",idProduct),HttpStatus.OK);
        }else {
            return new ResponseEntity<>(new ApiResponse<>(false,"Delete faild!",null),HttpStatus.BAD_REQUEST);
        }
    }
}



