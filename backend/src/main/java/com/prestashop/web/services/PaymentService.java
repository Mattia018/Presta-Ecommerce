package com.prestashop.web.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import jakarta.transaction.Transactional;


@Service
public class PaymentService {
	
	@Transactional
	public PaymentIntent createPaymentIntent(double amount) {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", (long) (amount * 100));
        params.put("currency", "eur");

        try {
            return PaymentIntent.create(params);
        } catch (StripeException e) {
            
            throw new RuntimeException("Errore durante la creazione del PaymentIntent: " + e.getMessage(), e);
        }
    }
}