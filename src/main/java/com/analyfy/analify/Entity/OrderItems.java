package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.OneToMany;

public class OrderItems extends EntityBase<Long>{
    private Double price;
    private Integer quantity;
    private Double discount = null;


    @OneToMany(mappedBy="orderitem")
    private List<Product> product;
    
    @OneToMany(mappedBy="orderitem")
    private List<Order> order;
}
