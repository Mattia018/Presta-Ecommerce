package com.prestashop.web.domain;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Entity
@EqualsAndHashCode(callSuper = false)

public class Category implements Serializable {
	
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long sequId;
	
	@Column(columnDefinition = "VARCHAR(50)", nullable = false, unique = true)
	private String title;

	
	@OneToMany(mappedBy = "category")
	@JsonIgnore
	private List<Product>products= new ArrayList<>() ;
	
	
	public Category() {}
	
	
    public Category(String title) {
        this.title = title;
    }
    
    
}


