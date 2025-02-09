package com.example.shoppecommerce.Repository;

import com.example.shoppecommerce.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAll(Pageable pageable);

    default Page<Product> findAllNewsest(Pageable pageable) {
        return findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id")));
    }

    Page<Product> findByCategoryName(String categoryName, Pageable pageable);
    Page<Product> findByNameContainingAndPriceLessThanEqual(String keyword, double maxPrice, Pageable pageable);
    Page<Product> findByNameContainingAndPriceLessThanEqualAndCategoryName(String keyword, double maxPrice, String category, Pageable pageable);
    List<Product> findByNameContainingIgnoreCase(String name);
}
