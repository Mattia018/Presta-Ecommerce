package com.prestashop.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prestashop.web.domain.Order;
import com.prestashop.web.domain.enumeration.Status;
import com.prestashop.web.repository.OrderRepository;
import com.prestashop.web.services.OrderService;
import com.prestashop.web.services.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
	
	@Autowired
    private PaymentService paymentService;
	
	@Autowired
    private OrderService orderService;
	
	@Autowired
    private OrderRepository orderRepository;
	
	
	

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Object> requestBody) {
        
        double totalAmount = ((Number) requestBody.get("totalAmount")).doubleValue();
        
        // Creazione del PaymentIntent con più metodi di pagamento
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount((long) (totalAmount * 100)) 
            .setCurrency("eur") 
            
            .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true) 
                        .build()
                )
                .build();
        
        // Crea il PaymentIntent 
        PaymentIntent paymentIntent = paymentService.createPaymentIntent(totalAmount);

        
        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", paymentIntent.getClientSecret());

        return ResponseEntity.ok(response);
    }
    
   
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> requestBody) {
        
        Long orderId = ((Number) requestBody.get("orderId")).longValue();
        

        // Recupera l'ordine dal database
        Order order = orderService.getOrderById(orderId);

        
        // Aggiorna lo stato dell'ordine a "PAID"
        order.setStatus(Status.PAID);
        
        // Conferma l'ordine (decrementa lo stock e aggiorna lo stato a CONFIRMED)
        orderService.confirmOrder(orderId);
        
        orderRepository.save(order);

        return ResponseEntity.ok().build();
    }
    
}
