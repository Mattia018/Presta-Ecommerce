package com.prestashop.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prestashop.web.services.ProductRecommendationService;

@RestController
@RequestMapping("/api/recommendations")
public class ProductRecommendationController {
	
	@Autowired
    private ProductRecommendationService recommendationService;

    @PostMapping
    public ResponseEntity<String> getRecommendation(@RequestBody String query) {
    	 String jsonResponse = recommendationService.recommendProducts(query);

    	    return ResponseEntity.ok()
    	        .contentType(MediaType.APPLICATION_JSON)
    	        .body(jsonResponse);
    }
}
