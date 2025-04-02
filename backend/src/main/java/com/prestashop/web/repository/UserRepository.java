package com.prestashop.web.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prestashop.web.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional<User> findById(Long id);
	
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
}
