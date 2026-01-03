package com.analyfy.analify.DTO;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class OrderDTO {
    private Long orderId;
    private LocalDate orderDate;
    private LocalDate shipDate;

    // Who sold it?
    private Long cashierId;
    private String cashierName;
    
    // Where was it sold?
    private Long storeId;
    private String storeName;

    // Financials
    private Double totalAmount; // Calculated sum of items
    private Integer totalItems; // Count of items
    
    // Details
    private List<OrderItemDTO> items;
}