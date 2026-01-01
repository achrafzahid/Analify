package com.analyfy.analify.DTO;

import lombok.Data;

@Data
public class FaceDTO {
    private Long faceId;
    private String faceName;
    private String description;
    
    // Info Rang
    private Long rangId;
    private String rangName;
    
    // Info Category
    private Long categoryId;
    private String categoryName;
    
    // Statistiques
    private Integer totalSections;
    private Integer openSections;
    private Integer closedSections;
}