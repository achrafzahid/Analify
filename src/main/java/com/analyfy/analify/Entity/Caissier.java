package com.analyfy.analify.Entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "caissier")
@Getter @Setter
public class Caissier extends User {

    @Column(name = "date_started")
    private LocalDate dateStarted;
    
    private Double salaire;

    // Connection 1: The Store this cashier works at
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    // Connection 2: The Orders this cashier has processed
    // Mapped by the "caissier" field in the Order class
    @OneToMany(mappedBy = "caissier")
    private List<Order> orders;
}