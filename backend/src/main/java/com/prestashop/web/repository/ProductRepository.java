package com.prestashop.web.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prestashop.web.domain.Category;
import com.prestashop.web.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>{
	
	Optional<Product> findById(Long id); 
	
    List<Product> findByCategory(Category category);
    
    Page<Product>findByCategory(Category category,Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
    Page<Product>findByDeleteAtIsNull(Pageable pageable);
    
    List<Product>findByDeleteAtIsNull();
    
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
    
    Page<Product> findByDeleteAtIsNullAndTitleContainingIgnoreCase(String title, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.deleteAt IS NULL")
    long countByDeleteAtIsNull();
    
    
}
