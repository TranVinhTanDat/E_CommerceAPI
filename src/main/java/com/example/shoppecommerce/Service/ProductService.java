package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.Entity.Product;
import com.example.shoppecommerce.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getAllProductsPage(int page, int size, String category) {
        PageRequest pageable = PageRequest.of(page, size);
        if (category == null || category.isEmpty()) {
            return productRepository.findAll(pageable);
        } else {
            return productRepository.findByCategoryName(category, pageable);
        }
    }

    // Phân trang với sản phẩm mới nhất trước
    public Page<Product> getAllProductsPageAdmin(int page, String category) {
        PageRequest pageable = PageRequest.of(page, 10); // Mỗi trang hiển thị 10 sản phẩm
        if (category == null || category.isEmpty()) {
            return productRepository.findAllNewsest(pageable); // Lấy sản phẩm mới nhất
        } else {
            return productRepository.findByCategoryName(category, pageable); // Lấy sản phẩm theo danh mục
        }
    }

    public Page<Product> searchProducts(int page, int size, String keyword, double maxPrice, String category) {
        PageRequest pageable = PageRequest.of(page, size);
        if (category == null || category.isEmpty()) {
            return productRepository.findByNameContainingAndPriceLessThanEqual(keyword, maxPrice, pageable);
        } else {
            return productRepository.findByNameContainingAndPriceLessThanEqualAndCategoryName(keyword, maxPrice, category, pageable);
        }
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }
}
