package com.analyfy.analify.Entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Caissier extends User {

    @ManyToOne(fetch=FetchType.LAZY)
    private Store store;

    @OneToMany(mappedBy="caissier")
    private List<Order> orders;
    
    private Date date_started;
    private Double salary;
}
