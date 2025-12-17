package com.analyfy.analify.DTO;


import lombok.Data;


@Data
public class ProductDTO {
    private Long productId;
    private String productName;
    
    // Flattened Category Info for easy filtering
    private Long subId;
    private String subName;
    private String categoryName;
}