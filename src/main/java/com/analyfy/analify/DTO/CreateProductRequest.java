package com.analyfy.analify.DTO;

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
}