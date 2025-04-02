package com.prestashop.web.services;




import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prestashop.web.domain.Product;
import com.prestashop.web.repository.ProductRepository;
import com.prestashop.web.request.OllamaRequest;
import com.prestashop.web.request.OllamaResponse;
import com.prestashop.web.request.ProductResponse;
import com.prestashop.web.request.RecommendationResponse;

@Service
public class ProductRecommendationService {
	
    @Autowired
    private ProductRepository productRepository;

    private final String OLLAMA_API_URL = "http://localhost:11434/api/generate";

    public String recommendProducts(String userQuery) {
        try {
            List<Product> products = productRepository.findByDeleteAtIsNull();

           
            String productsDescription = products.stream()
                .limit(30)
                .map(product -> String.format(
                	"- %s: %s (Category: %s, Price: %.2f)",
                    product.getTitle(),
                    product.getDescription(),
                    product.getCategory().getTitle(),
                    product.getPrice()
                    
                ))
                .collect(Collectors.joining("\n"));

            // Prompt 
            String prompt = String.format(
            		"You are a helpful and kind shopping assistant, The user is looking for: '%s'.\n" +
            			    "Your task is to recommend ONLY from the products listed below.\n" +
            			    "Give priority to the most relevant products and suggest a maximum of 3.\n" +
            			    "If no relevant products are found, inform the user and suggest 3 random products instead.\n\n" +
            			    "IMPORTANT: DO NOT include entire product descriptions in your response.Ever explain your advices in few rows. \n" +
            			    "Here are the available products:\n%s",
                userQuery,
                productsDescription
            );

            //System.out.println("Prompt sent to Phi4: " + prompt);

            // Chiamata API al modello
            RestTemplate restTemplate = getRestTemplateWithTimeout();
            OllamaRequest request = new OllamaRequest("gemma3:4b", prompt, false);
            
            OllamaResponse response = restTemplate.postForObject(OLLAMA_API_URL, request, OllamaResponse.class);
            
            //System.out.println("response Phi3: " + response);
            
            // Otteniamo i prodotti menzionati dal modello
            List<Product> recommendedProducts = products.stream()
                .filter(product -> response.getResponse().toLowerCase().contains(product.getTitle().toLowerCase()))
                .collect(Collectors.toList());

            // Se non ci sono corrispondenze, scegliamo 3 prodotti casuali
            if (recommendedProducts.isEmpty()) {
                Collections.shuffle(products);
                recommendedProducts = products.stream().limit(3).collect(Collectors.toList());
            }

            // Creazione della risposta JSON
            RecommendationResponse recommendationResponse = new RecommendationResponse(
                response.getResponse(),
                recommendedProducts.stream()
                    .map(product -> new ProductResponse(
                        product.getSequId(),
                        product.getTitle(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getImgResources(),
                        product.getCategory().getTitle()
                    ))
                    .collect(Collectors.toList())
            );

            ObjectMapper objectMapper = new ObjectMapper();
            
            return objectMapper.writeValueAsString(recommendationResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to generate recommendation: " + e.getMessage() + "\"}";
        }
    }
    
    private RestTemplate getRestTemplateWithTimeout() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(40000); 
        factory.setReadTimeout(40000);    
        return new RestTemplate(factory);
    }

    

   

}
