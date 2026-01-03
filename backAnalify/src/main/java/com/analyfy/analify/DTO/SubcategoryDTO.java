package com.analyfy.analify.DTO;

import lombok.Data;


@Data
public class SubcategoryDTO {
    private Long subId;
    private String subName;
    
    private Long categoryId;
    private String categoryName;
}