package com.analyfy.analify.DTO.Bids;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class CreateBidRequest {
    @NotNull(message = "L'ID de la section est requis")
    private Long sectionId;
    
    @NotNull(message = "L'ID de l'investisseur est requis")
    private Long investorId;
    
    @NotNull(message = "Le montant est requis")
    @Positive(message = "Le montant doit être positif")
    private Double amount;
}