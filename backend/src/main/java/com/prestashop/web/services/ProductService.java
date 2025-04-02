package com.prestashop.web.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.prestashop.web.domain.Category;
import com.prestashop.web.domain.Product;
import com.prestashop.web.repository.CartItemRepository;
import com.prestashop.web.repository.CategoryRepository;
import com.prestashop.web.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ProductService {

	@Autowired
    private ProductRepository productRepository;
	
	@Autowired
    private CategoryRepository categoryRepository;
	
	@Autowired
    private CartItemRepository cartItemRepository;

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> findAll() {
    	
        return productRepository.findByDeleteAtIsNull();
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }
    
    @Transactional
    public void deleteById(Long id) {
    	
    	Product product = findById(id).orElseThrow( ()->
    			 new IllegalArgumentException("Prodotto non trovato")
    			);
    	
    	product.delete();    	
        productRepository.save(product);
    }

    public Page<Product> findByCategory(Long categoryId, Pageable pageable) {
    	Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
    	Category category = optionalCategory.get();
        return productRepository.findByCategory(category, pageable);
    }
    
    
    public Page<Product> findAllProductShop(Pageable pageable) {
    	
        return productRepository.findByDeleteAtIsNull(pageable);
    }
    
    public Page<Product> findAllProductShopFilter(
            String category, Double minPrice, Double maxPrice, String search, String sortBy, String sortDirection, Pageable pageable) {

            // Specifica per filtrare i prodotti non eliminati
            Specification<Product> spec = Specification.where((root, query, cb) -> cb.isNull(root.get("deleteAt")));

            // Aggiungi filtri
            if (category != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("title"), category));
            }
            if (minPrice != null) {
                spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (search != null) {
                spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
            }

            // Ordinamento
            if (sortBy != null && sortDirection != null) {
                pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
                );
            }

            
            return productRepository.findAll(spec, pageable);
        }
    
    @Transactional
    public Product createProduct(String title, String description, String imgResources, String imgResources2, String imgResources3, double price, int stock, Long categoryId) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo del prodotto non può essere vuoto");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Il prezzo deve essere maggiore di zero");
        }

        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setImgResources(imgResources);
        product.setImgResources2(imgResources2);
        product.setImgResources3(imgResources3);
        product.setPrice(price);
        product.setStock(stock);

        if (categoryId != null) {
            Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
            if (optionalCategory.isEmpty()) {
                throw new EntityNotFoundException("Categoria con ID " + categoryId + " non trovata");
            }
            product.setCategory(optionalCategory.get());
        }

        return productRepository.save(product);
    }
    
    /**
     * Aggiorna un prodotto esistente.
     */
    @Transactional
    public Product updateProduct(Long productId, String title, String description, String imgResources, String imgResources2, String imgResources3, double price, int stock, Long categoryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Prodotto non trovato con ID: " + productId));

        product.setTitle(title);
        product.setDescription(description);
        product.setImgResources(imgResources);
        product.setImgResources2(imgResources2);
        product.setImgResources3(imgResources3);
        product.setPrice(price);
        product.setStock(stock);

        if (categoryId != null) {
            Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
            if (optionalCategory.isEmpty()) {
                throw new EntityNotFoundException("Categoria con ID " + categoryId + " non trovata");
            }
            product.setCategory(optionalCategory.get());
        }

        return productRepository.save(product);
    }
    
    /**
     * Rimuove un prodotto dal database.
     */
    @Transactional
    public void deleteProduct(Long productId) {
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Prodotto non trovato con ID: " + productId));        
        cartItemRepository.deleteAllByProduct(product);        
        productRepository.delete(product);
    }
    
    public Page<Product> searchProductsByName(String title, Pageable pageable) {
        return productRepository.findByDeleteAtIsNullAndTitleContainingIgnoreCase(title, pageable);
    }
    
    // Metodo per contare il numero di prodotti con deleteAt null.
    public long countProductsNotDeleted() {
        return productRepository.countByDeleteAtIsNull();
    } 
    
}   
