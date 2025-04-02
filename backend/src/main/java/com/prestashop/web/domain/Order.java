package com.prestashop.web.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.prestashop.web.domain.enumeration.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "order_table")
public class Order implements Serializable{
	
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long sequId;
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@OneToOne
	@JoinColumn(name = "cart_sequId")
	private Cart cart;
	
	@ManyToOne
	@JoinColumn(name = "user_sequId")
	private User user;
	
	private double totalPrice;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();	
	
	@Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt; 
	
	@Column
    private String shippingAddress;	
	
	@Column
    private String userEmail; 
	
	@Column
    private int productCount;
    
    
    
	public Order() {}
	
	    
	public Order(Status status, User user, double totalPrice, List<CartItem> items, String shippingAddress,String userEmail, int productCount,LocalDateTime createdAt) {		
        this.status = status;
        this.user = user;
        this.totalPrice = totalPrice;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.userEmail = userEmail;
        this.productCount = productCount;
        this.createdAt = createdAt;
    }

	public String getUserEmail() { return userEmail; }
	
	public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

	public int getProductCount() { return productCount; }
	
	public void setProductCount(int productCount) { this.productCount = productCount; }
	  
}
