package com.analyfy.analify.DTO;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String userName;
    private String mail;
    private LocalDate dateOfBirth;
}