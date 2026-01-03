package com.analyfy.analify.DTO.Bids;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class CreateBidRequest {
    @NotNull(message = "L'ID de la section est requis")
    private Long sectionId;
    
    // InvestorId will be set by the controller from JWT token
    // No validation needed as it's not sent by the client
    private Long investorId;
    
    @NotNull(message = "Le montant est requis")
    @Positive(message = "Le montant doit être positif")
    private Double amount;
}