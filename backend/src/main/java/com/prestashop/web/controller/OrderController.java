package com.prestashop.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prestashop.web.domain.Order;
import com.prestashop.web.repository.OrderRepository;
import com.prestashop.web.services.OrderService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	@Autowired
    private OrderService orderService;
	
	@Autowired
    private OrderRepository orderRepository;
	
	/**
     * Crea un ordine a partire dal carrello dell'utente.
     */
	@PostMapping	
	public ResponseEntity<Order> createOrder(@RequestBody Map<String, Object> requestBody) {
	    Long userId = ((Number) requestBody.get("userId")).longValue();
	    String shippingAddress = (String) requestBody.get("shippingAddress");

	    if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
	        throw new IllegalArgumentException("L'indirizzo di spedizione non può essere vuoto");
	    }

	    Order order = orderService.createOrder(userId, shippingAddress);
	    return ResponseEntity.status(HttpStatus.CREATED).body(order);
	}
    
    @GetMapping
    
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Contrassegna un ordine come pagato (status PAID).
     */
    @PutMapping("/{orderId}/paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> confirmPayment(@PathVariable(name="orderId") Long orderId) {        
        orderService.markOrderAsPaid(orderId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Contrassegna un ordine come spedito (status SHIPPED).
     */
    @PutMapping("/{orderId}/shipped")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAsShipped(@PathVariable(name="orderId") Long orderId) {
        orderService.markOrderAsShipped(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Contrassegna un ordine come consegnato (status DELIVERED).
     */
    @PutMapping("/{orderId}/delivered")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAsDelivered(@PathVariable(name="orderId") Long orderId) {
        orderService.markOrderAsDelivered(orderId);
        return ResponseEntity.ok().build();
    }
    
    

    /**
     * Conferma un ordine (status CONFIRMED) e decrementa lo stock.
     */
    @PutMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> confirmOrder(@PathVariable(name = "orderId") Long orderId) {
        orderService.confirmOrder(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Cancella un ordine e restaura lo stock dei prodotti.
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable(name = "orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }


    /**
     * Ottiene un ordine tramite ID.    
    }*/    
    @GetMapping("/{id}")
    public ResponseEntity<Page<Order>> getOrderItems(
            @PathVariable(name="id") Long id,
            Pageable pageable) {
        Page<Order> items = orderService.getOrderItemsByOrderId(id, pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Ottiene tutti gli ordini di un utente.     
    */    
    @GetMapping("/user/{userId}")
    public Page<Order> getOrdersByUserPaged(
        @PathVariable(name="userId") Long userId,
        @RequestParam(name="page",defaultValue = "0") int page,
        @RequestParam(name="size",defaultValue = "5") int size,
        @RequestParam(name="status",required = false) String status,
        @RequestParam(name="sortBy",defaultValue = "createdAt") String sortBy,
        @RequestParam(name="sortDirection",defaultValue = "DESC") String sortDirection) {
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
      return orderService.getOrdersByUserPaged(userId, pageable, status);
    }
    
    
    /**
     * Ottiene tutti gli ordini di tutti gli utenti.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Order>> getAllOrders(
        @RequestParam(name="status",required = false) String status,
        @RequestParam(name="startDate",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(name="endDate",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(name="searchEmail",required = false) String searchEmail,
        @SortDefault.SortDefaults({
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
        }) Pageable pageable) {

        Page<Order> orders = orderService.filterOrders(status, startDate, endDate, searchEmail, pageable);
        return ResponseEntity.ok(orders);
    }
    
    // Endpoint per ottenere le top 3 categorie
    @GetMapping("/top-categories")
    public List<Map<String, Object>> getTopCategories() {
        return orderService.getTopCategories(3);
    }

    // Endpoint per ottenere i top 3 prodotti
    @GetMapping("/top-products")
    public List<Map<String, Object>> getTopProducts() {
        return orderService.getTopProducts(3);
    }
    
    // Endpoint per ottenere i degli ordini di un utente
    @GetMapping("/{userId}/orders-count")
    public ResponseEntity<Long> getCompletedOrdersCountByUser(@PathVariable(name="userId") Long userId) {
        try {
            
            long count = orderService.getCompletedOrdersCountByUser(userId);
            
            return ResponseEntity.ok(count);
        } catch (EntityNotFoundException ex) {
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    // Endpoint per contare il numero di ordini con status CONFIRMED, SHIPPED, DELIVERED.
    @GetMapping("/admin/count-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countOrdersByConfirmedShippedDelivered() {
        long orderCount = orderService.countOrdersByConfirmedShippedDelivered();
        return ResponseEntity.ok(orderCount);
    }
    
    // Endpoint per aveve la somma totale del costo degli ordini
    @GetMapping("/admin/total-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Double> getTotalRevenueForConfirmedShippedDelivered() {
        double totalRevenue = orderService.calculateTotalRevenueForConfirmedShippedDelivered();
        return ResponseEntity.ok(totalRevenue);
    }
    
    // Endpoint per avere l andamento delle vendite nel mese corrente
    @GetMapping("/admin/daily-sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getDailySales() {
        return ResponseEntity.ok(orderService.getDailySalesForCurrentMonth());
    }
    
}
