package com.prestashop.web.request;

import java.util.List;

import lombok.Data;

@Data
public class RecommendationResponse {
	
	private String recommendationText;
    private List<ProductResponse> products;

    public RecommendationResponse(String recommendationText, List<ProductResponse> products) {
        this.recommendationText = recommendationText;
        this.products = products;
    }

}
