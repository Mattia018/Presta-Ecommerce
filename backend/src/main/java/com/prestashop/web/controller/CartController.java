package com.prestashop.web.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prestashop.web.domain.Cart;
import com.prestashop.web.services.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

	@Autowired
	private CartService cartService;

	/**
	 * Aggiunge un prodotto al carrello di un utente.
	 */
	@PostMapping("/add")
	public ResponseEntity<Void> addProductToCart(@RequestBody Map<String, Object> request) {
		Long userId = Long.valueOf((Integer) request.get("userId"));
		Long productId = Long.valueOf((Integer) request.get("productId"));
		int quantityToAdd = (Integer) request.getOrDefault("quantity", 1); // Default: aggiungi 1 unità

		cartService.addProductToCart(userId, productId, quantityToAdd);
		return ResponseEntity.ok().build();
	}

	/**
	 * Rimuove un prodotto dal carrello di un utente.
	 */
	@PostMapping("/remove")
	public ResponseEntity<Void> removeProductFromCart(@RequestBody Map<String, Object> request) {
		Long userId = Long.valueOf((Integer) request.get("userId"));
		Long productId = Long.valueOf((Integer) request.get("productId"));
		Integer quantityToRemove = (Integer) request.get("quantity"); // Opzionale

		cartService.removeProductFromCart(userId, productId, quantityToRemove);
		return ResponseEntity.ok().build();
	}
	
	/**
     * Aggiorna le unità di prodotto del carrello di un utente.
     */
	@PutMapping("/updateQuantity")
	public ResponseEntity<Void> updateProductQuantity(@RequestBody Map<String, Object> request) {
	    Long userId = Long.valueOf((Integer) request.get("userId"));
	    Long productId = Long.valueOf((Integer) request.get("productId"));
	    int newQuantity = (Integer) request.get("quantity");

	    cartService.updateProductQuantity(userId, productId, newQuantity);
	    return ResponseEntity.ok().build();
	}

	
	/**
	 * Ottiene i prodotti nel carrello di un utente.
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<Cart> getProductsInCart(@PathVariable(name = "userId") Long userId) {
		Cart cartContents = cartService.getProductsInCart(userId);
		return ResponseEntity.ok(cartContents);
	}

	/**
	 * Ottiene prezzo totale dei prodotti nel carrello di un utente.
	 */
	@GetMapping("/total/{userId}")
	public ResponseEntity<Double> getTotalPriceInCart(@PathVariable(name = "userId") Long userId) {
		double totalPrice = cartService.getTotalPriceInCart(userId);
		return ResponseEntity.ok(totalPrice);
	}

}
