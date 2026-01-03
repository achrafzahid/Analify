package com.analyfy.analify.DTO.StockOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRevenueDTO {
    private Long productId;
    private String productName;
    private Double totalRevenue;
    private Long totalQuantitySold;
}