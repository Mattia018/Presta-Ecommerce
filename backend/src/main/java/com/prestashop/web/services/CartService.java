package com.prestashop.web.services;



import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prestashop.web.domain.Cart;
import com.prestashop.web.domain.CartItem;
import com.prestashop.web.domain.Product;
import com.prestashop.web.domain.User;
import com.prestashop.web.repository.CartItemRepository;
import com.prestashop.web.repository.CartRepository;
import com.prestashop.web.repository.ProductRepository;
import com.prestashop.web.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class CartService {
	
	@Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CartItemRepository cartItemRepository;


    /**
     * Aggiunge un prodotto al carrello di un utente.
     */
    @Transactional
    public void addProductToCart(Long userId, Long productId, int quantityToAdd) {
    	
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Prodotto non trovato con ID: " + productId));
        
        // Controlla che la quantità richiesta sia disponibile in stock
        if (product.getStock() < quantityToAdd) {
            throw new IllegalStateException("Stock insufficiente per il prodotto: " + product.getTitle());
        }
        
        Cart cart = user.getCart();
        if (cart == null) {
            throw new IllegalStateException("Carrello non disponibile per l'utente con ID: " + userId);
        }

        // Controlla se il prodotto è già presente nel carrello
        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getSequId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            // Aggiorna la quantità esistente 
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantityToAdd);

            // Salva oggetto CartItem
            cartItemRepository.save(cartItem);
        } else {
            // Crea un nuovo elemento del carrello
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantityToAdd);
            
            cart.getItems().add(cartItem);

            // Salva l'oggetto CartItem
            cartItemRepository.save(cartItem);
        }
        
        userRepository.save(user);
    }
    
    
    /**
     * Rimuove un prodotto dal carrello di un utente.
     */
    @Transactional
    public void removeProductFromCart(Long userId, Long productId, Integer quantityToRemove) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        Cart cart = user.getCart();
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Carrello vuoto per l'utente con ID: " + userId);
        }

        // Trova l'elemento del carrello corrispondente
        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getSequId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();

            if (quantityToRemove != null && quantityToRemove > 0) {
                // Caso 1: Rimozione parziale (rimuovi una o più unità)
                int newQuantity = cartItem.getQuantity() - quantityToRemove;

                if (newQuantity > 0) {
                    // Aggiorna la quantità se rimangono unità
                    cartItem.setQuantity(newQuantity);

                    // Salva l'oggetto CartItem
                    cartItemRepository.save(cartItem);
                } else {
                    // Caso 2: Quantità totale <= 0 → Rimuovi completamente il prodotto
                    cart.getItems().remove(cartItem);

                    // Elimina l'oggetto CartItem
                    cartItemRepository.delete(cartItem);
                }
            } else {
                // Caso 3: Rimozione completa del prodotto (nessuna quantità specificata)
                cart.getItems().remove(cartItem);

                // Elimina esplicitamente l'oggetto CartItem
                cartItemRepository.delete(cartItem);
            }
        } else {
            throw new IllegalArgumentException("Prodotto non trovato nel carrello dell'utente con ID: " + userId);
        }

        // Salva le modifiche all'utente
        userRepository.save(user);
        
    }
    
    /**
     * Aggiorna le unità di prodotto del carrello di un utente.
     */
    @Transactional
    public void updateProductQuantity(Long userId, Long productId, int newQuantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Prodotto non trovato con ID: " + productId));

        // Controlla che la nuova quantità sia disponibile in stock
        if (product.getStock() < newQuantity) {
            throw new IllegalStateException("Stock insufficiente per il prodotto: " + product.getTitle());
        }

        Cart cart = user.getCart();
        if (cart == null) {
            throw new IllegalStateException("Carrello non disponibile per l'utente con ID: " + userId);
        }

        // Trova l'elemento del carrello corrispondente al prodotto
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getSequId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Prodotto non trovato nel carrello"));

        // Aggiorna la quantità
        cartItem.setQuantity(newQuantity);

        // Salva l'elemento del carrello aggiornato
        cartItemRepository.save(cartItem);

        
        userRepository.save(user);
    }
 
    /**
     * Restituire i prodotti dal carrello di un utente.
     */    
    public Cart getProductsInCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        Cart cart = user.getCart();
        

        return cart;
    }

    public double getTotalPriceInCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        Cart cart = user.getCart();
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return 0.0;
        }

        
        double totalPrice = cart.getItems().stream()
                .mapToDouble(cartItem -> cartItem.getProduct().getPrice() * cartItem.getQuantity())
                .sum();

        return totalPrice;
    }

}
