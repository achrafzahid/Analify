package com.analyfy.analify.DTO;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class CaissierDTO extends UserDTO {
    private LocalDate dateStarted;
    private Double salaire;

    // Flattened Store Info (No nested objects)
    private Long storeId;
    private String storeName; 
}