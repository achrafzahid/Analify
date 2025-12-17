package com.analyfy.analify.DTO;

import lombok.Data;


@Data
public class OrderItemDTO {
    private Long itemId;
    
    // Product Info
    private Long productId;
    private String productName;
    private String categoryName; // Useful for "Sales by Category" reports

    // Financials at the time of sale
    private Double price;
    private Double discount;
    private Integer quantity;
    
    private Double lineTotal; // (price - discount) * quantity
}