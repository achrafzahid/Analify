package com.analyfy.analify.DTO.StockOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSellingProductDTO {
    private Long productId;
    private String productName;
    private String categoryName;
    private Long totalQuantitySold;
    private Double totalRevenue;
}