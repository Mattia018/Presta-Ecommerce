package com.prestashop.web.request;

import lombok.Data;

@Data
public class ProductResponse {
	
	private Long sequId;
    private String title;
    private String description;
    private double price;
    private String imgResources;
    private String category;

    public ProductResponse(Long sequId, String title, String description, double price, String imgResources, String category) {
        this.sequId = sequId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imgResources = imgResources;
        this.category = category;
    }


}
