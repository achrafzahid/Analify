package com.analyfy.analify.DTO.StockOrder;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateProductRequest {
    @NotBlank(message = "Le nom du produit est requis")
    private String productName;
    
    @NotNull(message = "La sous-catégorie est requise")
    private Long subId;
    
    @NotNull(message = "L'investisseur est requis")
    private Long investorId;

    @NotNull(message="price required")
    private Double price;

    // 🆕 Add this field
    private Integer initialQuantity = 100; // Default to 0 if not sent
}