package com.prestashop.web.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prestashop.web.domain.Product;
import com.prestashop.web.services.ProductService;

@RestController
@RequestMapping("/api/products")

public class ProductController {

	@Autowired
    private ProductService productService;
	
	
	 
	/**
     * Crea un nuovo prodotto.
     */
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Product> createProduct(@RequestBody Map<String, Object> request) {
	    String title = (String) request.get("title");
	    String description = (String) request.get("description");
	    String imgResources = (String) request.get("imgResources");
	    String imgResources2 = (String) request.get("imgResources2");
	    String imgResources3 = (String) request.get("imgResources3");

	    Number priceNumber = (Number) request.get("price");
	    double price = (priceNumber != null) ? priceNumber.doubleValue() : 0.0;

	    int stock = ((Number) request.getOrDefault("stock", 1)).intValue();

	    Long categoryId = (request.get("categoryId") != null) 
	            ? ((Number) request.get("categoryId")).longValue() 
	            : null;

	    if (title == null || title.trim().isEmpty()) {
	        throw new IllegalArgumentException("Il titolo del prodotto non può essere vuoto");
	    }
	    if (price <= 0) {
	        throw new IllegalArgumentException("Il prezzo deve essere maggiore di zero");
	    }

	    Product createdProduct = productService.createProduct(title, description, imgResources, imgResources2, imgResources3, price, stock, categoryId);
	    return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
	}
	
	/**
	 * Aggiorna un prodotto esistente.
	 */
	@PutMapping("/{productId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Product> updateProduct(@PathVariable(name="productId") Long productId, @RequestBody Map<String, Object> request) {
	    String title = (String) request.get("title");
	    String description = (String) request.get("description");
	    String imgResources = (String) request.get("imgResources");
	    String imgResources2 = (String) request.get("imgResources2");
	    String imgResources3 = (String) request.get("imgResources3");

	    Number priceNumber = (Number) request.get("price");
	    double price = (priceNumber != null) ? priceNumber.doubleValue() : 0.0;

	    int stock = ((Number) request.getOrDefault("stock", 1)).intValue();

	    Long categoryId = (request.get("categoryId") != null) 
	            ? ((Number) request.get("categoryId")).longValue() 
	            : null;

	    if (title == null || title.trim().isEmpty()) {
	        throw new IllegalArgumentException("Il titolo del prodotto non può essere vuoto");
	    }
	    if (price <= 0) {
	        throw new IllegalArgumentException("Il prezzo deve essere maggiore di zero");
	    }

	    Product updatedProduct = productService.updateProduct(productId, title, description, imgResources, imgResources2, imgResources3, price, stock, categoryId);
	    return ResponseEntity.ok(updatedProduct);
	}
	

    /**
     * Ottiene un prodotto tramite ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable(name="id") Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Ottiene tutti i prodotti.
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }
    
    /**
     * Ottiene tutti i prodotti.
    */    
    @GetMapping("/shop")
    public Page<Product> findAllProductShop(
        @RequestParam(name="category",required = false) String category,
        @RequestParam(name="minPrice",required = false) Double minPrice,
        @RequestParam(name="maxPrice",required = false) Double maxPrice,
        @RequestParam(name="search",required = false) String search,
        @RequestParam(name="sortBy",required = false) String sortBy,
        @RequestParam(name="sortDirection",required = false) String sortDirection,
        Pageable pageable) {
        
        return productService.findAllProductShopFilter(category, minPrice, maxPrice, search, sortBy, sortDirection, pageable);
    }
    
    

    /**
     * Ottiene i prodotti in base alla categoria.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategory(@PathVariable(name="categoryId") Long categoryId, Pageable pageable) {
        Page<Product> products = productService.findByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Rimuove un prodotto dal database.
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable(name="productId") Long productId) {        
    	productService.deleteById(productId);
        return ResponseEntity.noContent().build();
    }
    
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Product>> getAllProducts(
        @RequestParam(name="page", defaultValue = "0") int page,
        @RequestParam(name="size",defaultValue = "10") int size,
        @RequestParam(name="sort",defaultValue = "title,asc") String sort,
        @RequestParam(name="search", required = false) String search) {
        
        // Estrai il campo e la direzione di ordinamento
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "asc";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        Page<Product> products;
        
        if (search != null && !search.isEmpty()) {       	
            products = productService.searchProductsByName(search, pageable);
        } else {
            products = productService.findAllProductShop(pageable);
        }
        
        
        return ResponseEntity.ok(products);
    }
    
    // Endpoint per ottenere il numero di prodotti con deleteAt null.
    @GetMapping("/admin/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countProductsNotDeleted() {
        long productCount = productService.countProductsNotDeleted();
        return ResponseEntity.ok(productCount);
    }
}
