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
    @RequestMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public class AdminProductController {
        @Autowired
        private ProductService productService;
        @PostMapping("/add-product")
        public ResponseEntity<ApiResponse<Product>> addProduct(@RequestBody Product product) {
            try {
                Product newProduct = productService.saveProduct(product);
                return new ResponseEntity<>(new ApiResponse<>(true,"Add product successfully", newProduct), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(new ApiResponse<>(false,"Add product Fail",null), HttpStatus.BAD_REQUEST);
            }
        }
        @PutMapping("/edit-product/{id}")
        public ResponseEntity<ApiResponse<Product>> editProduct(@PathVariable long id, @RequestBody Product product) {
            Product existProduct = productService.getProductById(id);
            if (existProduct != null) {
                product.setId(id);
                productService.saveProduct(product);
                return new ResponseEntity<>(new ApiResponse<>(true,"Edit product successfully", product), HttpStatus.OK);
            }else {
                return new ResponseEntity<>(new ApiResponse<>(false,"Edit product Fail",null), HttpStatus.BAD_REQUEST);
            }
        }

        @DeleteMapping("/delete-product")
        public ResponseEntity<ApiResponse<Product>> deleteProduct(@RequestParam long id) {
            Product existProduct = productService.getProductById(id);
            if (existProduct != null) {
                productService.deleteProduct(id);
                return new ResponseEntity<>(new ApiResponse<>(true,"Delete product successfully", existProduct), HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(new ApiResponse<>(false,"Delete product Fail",null), HttpStatus.BAD_REQUEST);
            }
        }

        // Tìm kiếm sản phẩm
        @GetMapping("/Search")
        public ResponseEntity<ApiResponse<List<Product>>> searchProduct(@RequestParam String keyword) {
            List<Product> products = productService.searchProducts(keyword);
            if (products.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>(false,"Product not found:"+keyword, null), HttpStatus.NOT_FOUND);
            }else {
                return new ResponseEntity<>(new ApiResponse<>(true, "Product found", products), HttpStatus.OK);
            }
        }

        // Lấy tất cả sản phẩm
        @GetMapping
        public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
            List<Product> products = productService.getAllProducts();
            return new ResponseEntity<>(new ApiResponse<>(true, "Product list retrieved successfully", products), HttpStatus.OK);
        }

        // Lấy tất cả sản phẩm với phân trang (admin)
        @GetMapping("/page")
        public Page<Product> getAllProductsPageAdmin(@RequestParam int page, @RequestParam int size, @RequestParam(required = false) String category) {
            return productService.getAllProductsPageAdmin(page, category);
        }
    }
