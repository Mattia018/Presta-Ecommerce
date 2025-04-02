package com.prestashop.web.request;

import lombok.Data;

@Data
public class OllamaResponse {
	private String model;
    private String response;
    private boolean done;
}
