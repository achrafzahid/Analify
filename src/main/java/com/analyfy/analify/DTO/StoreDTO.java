package com.analyfy.analify.DTO;

import lombok.Data;


@Data
public class StoreDTO {
    private Long storeId;
    
    // Location Context
    private Long cityId;
    private String cityName;
    private String regionName; // Useful for grouping in charts

    // Manager Context
    private Long managerId;
    private String managerName;

    // Analytics (Calculated fields)
    private Integer employeeCount; 
}