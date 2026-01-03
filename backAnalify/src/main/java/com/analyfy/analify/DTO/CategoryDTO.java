package com.analyfy.analify.DTO;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long categoryId;
    private String categoryName;
    
    // Statistiques
    private Integer totalRangs;
    private Integer totalFaces;
    private Integer totalSections;
    private Integer activeBids;
}