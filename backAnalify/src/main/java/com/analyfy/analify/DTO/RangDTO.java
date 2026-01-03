package com.analyfy.analify.DTO;

import lombok.Data;

@Data
public class RangDTO {
    private Long rangId;
    private String rangName;
    private String description;
    
    // Info Category
    private Long categoryId;
    private String categoryName;
    
    // Statistiques
    private Integer totalFaces;
    private Integer availableSections;
}