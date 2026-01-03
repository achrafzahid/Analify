package com.analyfy.analify.DTO;

import java.time.LocalDate;
import lombok.Data;

@Data
public class SectionDTO {
    private Long sectionId;
    private String sectionName;
    private Double basePrice;
    private Double currentPrice;
    private String status; // OPEN, OPEN-BIDDEN BY X, CLOSED
    private LocalDate dateDelai;
    private String description;
    
    // Info Face
    private Long faceId;
    private String faceName;
    
    // Info Rang
    private Long rangId;
    private String rangName;
    
    // Info Category
    private Long categoryId;
    private String categoryName;
    
    // Statistiques
    private Integer totalBids;
    private Integer uniqueBidders;
    
    // Gagnant (si CLOSED)
    private Long winnerInvestorId;
    private String winnerInvestorName;
    private Double winningAmount;
}