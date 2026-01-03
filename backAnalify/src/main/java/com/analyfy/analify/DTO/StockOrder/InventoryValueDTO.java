package com.analyfy.analify.DTO.StockOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryValueDTO {
    private Long productId;
    private String productName;
    private Integer totalQuantity;
    private Double estimatedValue;  // quantity * average selling price
}