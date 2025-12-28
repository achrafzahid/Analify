package com.analyfy.analify.DTO.StockOrder;

import lombok.Data;

@Data
public class UpdateProductRequest {
    private String productName;
    private Long subId;
    private Long investorId;

    // 🆕 Add Price here so it can be updated
    private Double price;

    // 🆕 New field for batch updating stock
    private Integer quantity;

}