package com.prestashop.web.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prestashop.web.domain.Cart;
import com.prestashop.web.domain.CartItem;
import com.prestashop.web.domain.Product;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	Optional<CartItem> findByProduct(Product product);
	List<CartItem> deleteAllByCart(Cart cart);
	void deleteAllByProduct(Product product);
}
