package com.analyfy.analify.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Le caissier ID est requis")
    private Long cashierId;
    
    @NotEmpty(message = "La commande doit contenir au moins un article")
    private List<OrderItemRequest> items;
}