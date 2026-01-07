package com.analyfy.analify.DTO.StockOrder;

import lombok.Data;

@Data
public class LowStockAlertDTO {
    private Long productId;
    private Long storeId;
    private String storeCity;
    private String productName;
    private Integer quantity;
}