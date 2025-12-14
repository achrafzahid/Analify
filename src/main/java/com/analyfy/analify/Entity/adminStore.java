package com.analyfy.analify.Entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class adminStore extends User {
    @OneToOne
    private Store store;
    private Date date_started;
    private Double salary;



    
}
