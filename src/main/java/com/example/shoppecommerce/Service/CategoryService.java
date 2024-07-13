package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.Entity.Category;
import com.example.shoppecommerce.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getParent() == null)
                .collect(Collectors.toList());
    }

    public Category getCategoryById(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.orElse(null);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);
        if (category != null) {
            category.setName(categoryDetails.getName());
            category.setParent(categoryDetails.getParent());
            category.setSubCategories(categoryDetails.getSubCategories());
            return categoryRepository.save(category);
        } else {
            return null;
        }
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
