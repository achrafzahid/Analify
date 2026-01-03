package com.analyfy.analify.DTO;

import java.time.LocalDate;

import com.analyfy.analify.Enum.UserRole;

import lombok.Data;

@Data
public class EmployeeCreateDTO {
    // User Basic Fields
    private String userName;
    private String mail;
    private String password;
    private LocalDate dateOfBirth;

    // Logic Fields
    private UserRole role;

    // Role Specific Fields
    private Long storeId;
    
    // We use "salary" as the common input key in JSON
    // The service will map this to 'salare' for Caissier or 'salary' for AdminStore
    private Double salary; 
    
    private LocalDate dateStarted;
}