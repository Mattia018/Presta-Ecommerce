package com.prestashop.web.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prestashop.web.domain.Category;
import com.prestashop.web.domain.Product;
import com.prestashop.web.repository.CategoryRepository;
import com.prestashop.web.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class CategoryService {
	
	@Autowired
    private CategoryRepository categoryRepository;
	
	@Autowired
    private ProductRepository productRepository;
	
	@Transactional
	public Category createCategory(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo della categoria non può essere vuoto");
        }

        Category category = new Category();
        category.setTitle(title);
        return categoryRepository.save(category);
    }
	
	@Transactional
	public Category updateCategory(Long categoryId, String newTitle) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if (optionalCategory.isEmpty()) {
            throw new EntityNotFoundException("Categoria con ID " + categoryId + " non trovata");
        }

        Category category = optionalCategory.get();
        category.setTitle(newTitle);
        return categoryRepository.save(category);
    }
	
	@Transactional
	public void deleteCategory(Long categoryId) {
       Category category =categoryRepository.findById(categoryId).orElseThrow(()->
        new EntityNotFoundException("Categoria con ID " + categoryId + " non trovata"));          
        	
        List<Product> products= productRepository.findByCategory(category);
        
        for(Product product : products){        	
	        	product.setCategory(null);
	        	productRepository.save(product);
        	}
               	
        categoryRepository.delete(category);
    }
	
	public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
	
	public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria con ID " + categoryId + " non trovata"));
    }
	
	public Map<Long, Long> getProductCountByCategory() {
        List<Category> categories = categoryRepository.findAll();
        Map<Long, Long> productCountByCategory = new HashMap<>();

        for (Category category : categories) {
            long count = productRepository.countByCategoryId(category.getSequId());
            productCountByCategory.put(category.getSequId(), count);
        }

        return productCountByCategory;
    }
	
	
	public long countCategories() {
        return categoryRepository.count();
    }
}
