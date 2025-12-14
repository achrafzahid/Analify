package com.analyfy.analify.Entity;

import java.util.Date;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends EntityBase<Long> {
    private String name;
    private String mail;
    private String password;
    private Date dateOfBirth;
}
