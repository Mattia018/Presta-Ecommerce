package com.prestashop.web.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.prestashop.web.domain.Cart;
import com.prestashop.web.domain.CartItem;
import com.prestashop.web.domain.Order;
import com.prestashop.web.domain.Product;
import com.prestashop.web.domain.User;
import com.prestashop.web.domain.enumeration.Status;
import com.prestashop.web.repository.CartRepository;
import com.prestashop.web.repository.OrderRepository;
import com.prestashop.web.repository.UserRepository;
import com.prestashop.web.specification.OrderSpecification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Service
public class OrderService {

	@Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductService productService; 

    @Autowired
    private CartRepository cartRepository;  
    
    @Autowired
    private EntityManager entityManager;
    
    
    /**
     * Crea un ordine a partire dal carrello dell'utente.
     */    
    @Transactional
    public Order createOrder(Long userId, String shippingAddress) {
        // Step 1: Recupera l'utente e il suo carrello
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));
        Cart cart = user.getCart();
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Carrello vuoto o non disponibile per l'utente con ID: " + userId);
        }

        // Verifica che tutti i prodotti nel carrello abbiano stock sufficiente
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Stock insufficiente per il prodotto: " + product.getTitle());
            }
        }

        // Step 2: Calcola il prezzo totale del carrello
        double totalPrice = cart.getTotal();

        // Step 3: Crea l'ordine con stato PENDING
        Order order = new Order();
        order.setStatus(Status.PENDING);
        order.setUser(user);
        order.setUserEmail(user.getEmail());
        order.setProductCount(cart.getItems().size());
        order.setTotalPrice(totalPrice);
        order.setShippingAddress(shippingAddress);

        // Step 4: OrderItem
        List<CartItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            CartItem orderItem = new CartItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice()); 
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        // Step 5: Salva l'ordine nel database
        order = orderRepository.save(order);

        // Step 6: Ripulisci il carrello dopo aver creato l'ordine
        for (CartItem cartItem : cart.getItems()) {
            cartItem.setCart(null); 
        }
        cart.getItems().clear();
        cartRepository.save(cart);

        return order;
    }
    
    /**
     * Conferma un ordine e decrementa lo stock dei prodotti.
     */
    @Transactional
    public void confirmOrder(Long orderId) {
        // Step 1: Recupera l'ordine
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Ordine non trovato con ID: " + orderId));

        if (!order.getStatus().equals(Status.PAID)) {
            throw new IllegalStateException("Solo gli ordini nello stato PAID possono essere confermati.");
        }

        // Step 2: Decrementa lo stock dei prodotti
        for (CartItem cartItem : order.getItems()) {
            Product product = cartItem.getProduct();
            int newStock = product.getStock() - cartItem.getQuantity();
            if (newStock < 0) {
                throw new IllegalStateException("Stock insufficiente per il prodotto: " + product.getTitle());
            }
            product.setStock(newStock);
            productService.save(product);
        }

        // Step 3: Aggiorna lo status dell'ordine a CONFIRMED
        order.setStatus(Status.CONFIRMED);
        orderRepository.save(order);
    }

    /**
     * Contrassegna un ordine come pagato (status PAID).
     */
    @Transactional
    public void markOrderAsPaid(Long orderId) {
        // Step 1: Recupera l'ordine
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Ordine non trovato con ID: " + orderId));

        // Step 2: Aggiorna lo status dell'ordine a PAID
        order.setStatus(Status.PAID);
        orderRepository.save(order);
    }
    
    /**
     * Contrassegna un ordine come spedito (status SHIPPED).
     */
    @Transactional
    public void markOrderAsShipped(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Ordine non trovato con ID: " + orderId));
        order.setStatus(Status.SHIPPED);
        orderRepository.save(order);
    }

    /**
     * Contrassegna un ordine come consegnato (status DELIVERED).
     */
    @Transactional
    public void markOrderAsDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Ordine non trovato con ID: " + orderId));
        order.setStatus(Status.DELIVERED);
        orderRepository.save(order);
    }
    
    

    /**
     * Cancella un ordine e restaura lo stock dei prodotti.
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // Step 1: Recupera l'ordine
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Ordine non trovato con ID: " + orderId));


        // Step 2: Restaura lo stock dei prodotti
        for (CartItem cartItem : order.getItems()) {
            Product product = cartItem.getProduct();
            int newStock = product.getStock() + cartItem.getQuantity();
            product.setStock(newStock);
            productService.save(product);
        }

        // Step 3: Aggiorna lo status dell'ordine a CANCELLED
        order.setStatus(Status.CANCELLED);
        orderRepository.save(order);
    }
    
    /**
     * Ottiene un ordine tramite ID.
     */    
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordine con ID " + id + " non trovato"));
    }

    public Page<Order> getOrderItemsByOrderId(Long id, Pageable pageable) {
        return orderRepository.findBysequId(id, pageable);
    }
    
    /**
     * Ottiene tutti gli ordini di un utente.     
     */
    public List<Order> getOrdersByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente con ID " + userId + " non trovato"));

        return orderRepository.findByUser(user);
    }
    
    public long getCompletedOrdersCountByUser(Long userId) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente con ID " + userId + " non trovato"));

        
        List<Status> completedStatuses = Arrays.asList(
            Status.CONFIRMED,
            Status.SHIPPED,
            Status.DELIVERED
        );

        
        return orderRepository.countByUserAndStatusIn(user, completedStatuses);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Page<Order> getAllOrdersPaged(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
    
    public Page<Order> filterOrders(
    	    String status,
    	    LocalDate startDate,
    	    LocalDate endDate,
    	    String searchEmail,
    	    Pageable pageable) {

    	    Specification<Order> spec = Specification.where(null);

    	    // Filtra per stato
    	    if (status != null) {
    	        spec = spec.and(OrderSpecification.hasStatus(status));
    	    }

    	    // Filtra per data inizio
    	    if (startDate != null) {
    	        spec = spec.and(OrderSpecification.createdAtAfter(startDate.atStartOfDay()));
    	    }

    	    // Filtra per data fine
    	    if (endDate != null) {
    	        spec = spec.and(OrderSpecification.createdAtBefore(endDate.plusDays(1).atStartOfDay()));
    	    }

    	    // Filtra per email
    	    if (searchEmail != null) {
    	        spec = spec.and(OrderSpecification.hasUserEmailLike(searchEmail));
    	    }

    	    return orderRepository.findAll(spec, pageable);
    	}
    
    
    public Page<Order> getOrdersByUserPaged(Long userId, Pageable pageable, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente con ID " + userId + " non trovato"));

        if (status != null && !status.isEmpty()) {
            // Filtra per stato se specificato
            return orderRepository.findByUserAndStatus(user, Status.valueOf(status), pageable);
        } else {
            // Altrimenti, restituisci tutti gli ordini
            return orderRepository.findByUser(user, pageable);
        }
    }
    
    
    public List<Map<String, Object>> getTopCategories(int limit) {
        String jpql = "SELECT c.sequId as id, c.title as name, COUNT(o) as totalSold " +
                      "FROM Order o " +
                      "JOIN o.items i " +
                      "JOIN i.product p " +
                      "JOIN p.category c " +
                      "WHERE o.status IN ('CONFIRMED', 'DELIVERED', 'SHIPPED') " +
                      "GROUP BY c.sequId, c.title " +
                      "ORDER BY COUNT(o) DESC";
        
        Query query = entityManager.createQuery(jpql);
        query.setMaxResults(limit);
        
        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> categories = new ArrayList<>();
        
        for (Object[] result : results) {
            Map<String, Object> category = new HashMap<>();
            category.put("id", result[0]);
            category.put("name", result[1]);
            category.put("totalSold", result[2]);
            categories.add(category);
        }
        
        return categories;
    }
    
    public List<Map<String, Object>> getTopProducts(int limit) {
        String jpql = "SELECT p.sequId as id, p.title as name, COUNT(o) as totalSold, p.price as price, p.imgResources as imgResources " +
                      "FROM Order o " +
                      "JOIN o.items i " +
                      "JOIN i.product p " +
                      "WHERE o.status IN ('CONFIRMED', 'DELIVERED', 'SHIPPED') " +
                      "GROUP BY p.sequId, p.title, p.price, p.imgResources " +
                      "ORDER BY COUNT(o) DESC";
        
        Query query = entityManager.createQuery(jpql);
        query.setMaxResults(limit);
        
        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> products = new ArrayList<>();
        
        for (Object[] result : results) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", result[0]);
            product.put("name", result[1]);
            product.put("totalSold", result[2]);
            product.put("price", result[3]);
            product.put("imgResources", result[4]);
            products.add(product);
        }
        
        return products;
    }
    
    //Metodo per contare il numero di ordini con status CONFIRMED, SHIPPED, DELIVERED.    
    public long countOrdersByConfirmedShippedDelivered() {
        List<Status> statuses = Arrays.asList(Status.CONFIRMED, Status.SHIPPED, Status.DELIVERED);
        return orderRepository.countByStatusIn(statuses);
    }
    
    // Metodo per aveve la somma totale del costo degli ordini
    public double calculateTotalRevenueForConfirmedShippedDelivered() {
        List<Status> statuses = Arrays.asList(Status.CONFIRMED, Status.SHIPPED, Status.DELIVERED);
        return orderRepository.sumTotalPriceByStatusIn(statuses);
    }
    
    // Metoodo per avere l andamento delle vendite nel mese corrente
    public List<Map<String, Object>> getDailySalesForCurrentMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth())
                                               .withHour(23).withMinute(59).withSecond(59);
        
        List<Object[]> results = orderRepository.countDailySales(startOfMonth, endOfMonth);

        return results.stream().map(obj -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", obj[0]);
            map.put("count", obj[1]);
            map.put("total", obj[2]);
            return map;
        }).collect(Collectors.toList());
    }
}
