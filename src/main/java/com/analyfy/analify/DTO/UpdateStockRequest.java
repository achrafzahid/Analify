package com.analyfy.analify.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class UpdateStockRequest {
    @NotNull(message = "Le store ID est requis")
    private Long storeId;
    
    @NotNull(message = "La quantité est requise")
    @Positive(message = "La quantité doit être positive")
    private Integer quantity;
}