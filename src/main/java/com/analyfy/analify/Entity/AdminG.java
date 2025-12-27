package com.analyfy.analify.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "adminG")
@Getter @Setter
//@DiscriminatorValue("ADMIN_G")
@PrimaryKeyJoinColumn(name = "user_id")
public class AdminG extends User {
    // Inherits ID and basic fields from User
    // Add specific fields here if your future specs require them
}