package com.analyfy.analify.DTO;

import java.time.LocalDate;

import lombok.Data;

@Data
public class EmployeeUpdateDTO {
    private String userName;
    private String mail;
    private String password;
    private LocalDate dateOfBirth;
    
    private Double salary;
    private Long storeId;
}