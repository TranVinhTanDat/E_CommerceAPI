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

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
