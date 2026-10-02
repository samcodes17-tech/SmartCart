package com.smartcart.service;

import com.smartcart.entity.Category;
import com.smartcart.repository.CategoryRepository;
import com.smartcart.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found"));
    }

    public void save(Category category) {
        categoryRepository.save(category);
    }

    public void delete(Long id) {

        if (productRepository.existsByCategoryId(id)) {
            throw new IllegalStateException("Cannot delete a category that still has products in it");
        }
        categoryRepository.deleteById(id);
    }
}
