package com.prestashop.web.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prestashop.web.domain.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{
	Optional<Category> findById(Long id);
    Optional<Category> findByTitle(String title);	
    
}
