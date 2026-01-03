package com.analyfy.analify.DTO;

import lombok.Data;

@Data
public class StateDTO {
    private Long stateId;
    private String name;
    
    // Parent Info
    private Long regionId;
    private String regionName;
}