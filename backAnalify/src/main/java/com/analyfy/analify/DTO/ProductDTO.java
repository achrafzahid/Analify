package com.analyfy.analify.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private Double price;
    private String categoryName;
    private Long subId; 
    private String subName; 
    private Long investorId;
    private String investorName;
    private Long quantity; 

    // 🟢 Updated Constructor (9 arguments now)
    public ProductDTO(Long productId, String productName, Double price, 
                      String categoryName, Long subId, String subName, 
                      Long investorId, String investorName, Long quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.categoryName = categoryName;
        this.subId = subId;         // 🟢 Now set correctly
        this.subName = subName;
        this.investorId = investorId;
        this.investorName = investorName;
        this.quantity = quantity;
    }
}