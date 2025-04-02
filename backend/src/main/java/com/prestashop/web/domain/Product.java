package com.prestashop.web.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "product")
public class Product implements Serializable{

	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long sequId;
	
	@Column(columnDefinition = "VARCHAR(50)")
	private String title;	
	
	@Column(columnDefinition = "VARCHAR(500)")
	private String description;
	
	@Column(columnDefinition = "VARCHAR(250)")
	private String imgResources;
	
	@Column(columnDefinition = "VARCHAR(250)")
    private String imgResources2;

    @Column(columnDefinition = "VARCHAR(250)")
    private String imgResources3;
	
	@Column(columnDefinition = "DOUBLE(10, 2)")
	private double price;
	
	@Column(columnDefinition = "INT")
	private int stock;
	
	@Column
	@JsonIgnore
	private LocalDateTime deleteAt;
	
	@ManyToOne
	private Category category;

	public boolean isDeleted() {		
		return deleteAt!=null;
	}
	
	public Product delete() {
		this.deleteAt= LocalDateTime.now();
		return this;
	}
	
	
}
