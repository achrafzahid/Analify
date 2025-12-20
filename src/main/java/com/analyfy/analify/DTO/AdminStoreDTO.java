package com.analyfy.analify.DTO;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class AdminStoreDTO extends UserDTO {
    private LocalDate dateStarted;
    private Double salary;
    
    // ID of the store they manage
    private Long managedStoreId;
    private String managedStoreName;
}