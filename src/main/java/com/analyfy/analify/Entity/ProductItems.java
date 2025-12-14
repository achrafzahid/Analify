package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.OneToMany;

public class ProductItems extends EntityBase<Long>{
    private Integer quanity;

    @OneToMany(mappedBy="productitem")
    private List<Product> product;

    @OneToMany(mappedBy="productitem")
    private List<Store> store;

    
    
}
