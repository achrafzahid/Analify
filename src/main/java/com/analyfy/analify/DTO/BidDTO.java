package com.analyfy.analify.DTO;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BidDTO {
    private Long bidId;
    private Double amount;
    private LocalDateTime bidTime;
    private String status; // WINNER, OUTBID
    
    // Info Section
    private Long sectionId;
    private String sectionName;
    private Double sectionBasePrice;
    private Double sectionCurrentPrice;
    private String sectionStatus;
    
    // Info Investisseur
    private Long investorId;
    private String investorName;
    private String investorEmail;
    
    // Info hiérarchique
    private String faceName;
    private String rangName;
    private String categoryName;
}