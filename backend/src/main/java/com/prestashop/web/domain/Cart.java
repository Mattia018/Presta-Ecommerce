package com.prestashop.web.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "cart")
public class Cart implements Serializable{

	private static final long serialVersionUID = 1L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long sequId;
	
	
	@OneToMany(mappedBy = "cart")
    private List<CartItem> items = new ArrayList<>();
	
	@OneToOne
	@JsonIgnore
    private User user;
		
	public double getTotal() {
		
		if (getItems().isEmpty()) {
			
			return 0.0;
		}
		double totalPrice = getItems().stream()
                .mapToDouble(cartItem -> cartItem.getProduct().getPrice() * cartItem.getQuantity())
                .sum();
		
		return totalPrice;
				
	}
	
	
}
