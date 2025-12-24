package com.analyfy.analify.DTO;

import java.time.LocalDate;

import com.analyfy.analify.Enum.UserRole;

import lombok.Data;

@Data
public class EmployeeResponseDTO {
    private Long userId;
    private String userName;
    private String mail;
    private LocalDate dateOfBirth;
    
    // Computed Field: Calculated based on which table the user was found in
    private UserRole role; 
    
    private Long storeId;
    private Double salary;
    private LocalDate dateStarted;
}