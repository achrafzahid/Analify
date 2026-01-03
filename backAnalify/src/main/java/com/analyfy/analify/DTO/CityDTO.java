package com.analyfy.analify.DTO;

import lombok.Data;
@Data
public class CityDTO {
    private Long cityId;
    private String name;
    
    // Hierarchical Context for Analytics
    private Long stateId;
    private String stateName;
    private String regionName; 
}