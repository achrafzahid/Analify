package com.analyfy.analify.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotBlank;

@Data
public class OrderItemRequest {
    /*@NotNull(message = "Le product ID est requis")
    private Long productId;*/

    @NotBlank(message = "Le nom du produit est requis")
    private String productName;
    
    @NotNull(message = "La quantité est requise")
    @Positive(message = "La quantité doit être positive")
    private Integer quantity;
    
    @PositiveOrZero(message = "Le discount doit être positif ou zéro")
    private Double discount; // Optionnel, null par défaut
}