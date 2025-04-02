package com.prestashop.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prestashop.web.domain.Category;
import com.prestashop.web.services.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

	@Autowired
    private CategoryService categoryService;
	
	
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Category> createCategory(@RequestBody Map<String, Object> request) {
	    String title = (String) request.get("title");

	    // Validazione del titolo
	    if (title == null || title.trim().isEmpty()) {
	        throw new IllegalArgumentException("Il titolo della categoria non può essere vuoto");
	    }

	    Category createdCategory = categoryService.createCategory(title);
	    return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
	}

    /**
     * Ottiene una categoria tramite ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable(name="id") Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Aggiorna una categoria esistente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(@PathVariable(name="id") Long id, @RequestParam(name="newTitle") String newTitle) {
        Category updatedCategory = categoryService.updateCategory(id, newTitle);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Elimina una categoria.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable(name="id") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Ottiene tutte le categorie.
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Ottiene count prodotti associati alla categoria.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<Long, Long>> getProductCountByCategory() {
        Map<Long, Long> productCountByCategory = categoryService.getProductCountByCategory();
        return ResponseEntity.ok(productCountByCategory);
    }
    
    // Endpoint per avere il count di Category
    @GetMapping("/admin/count")
    public ResponseEntity<Long> getCategoryCount() {
        long categoryCount = categoryService.countCategories();
        return ResponseEntity.ok(categoryCount);
    }
}
