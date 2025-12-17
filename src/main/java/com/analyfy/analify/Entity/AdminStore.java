package com.analyfy.analify.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin_store")
@Getter @Setter
public class AdminStore extends User {

    @Column(name = "date_started")
    private LocalDate dateStarted;
    
    private Double salaire;

    // Bidirectional: The store this admin manages
    // Note: The diagram shows admin_store has a "store_id" column
    @OneToOne
    @JoinColumn(name = "store_id")
    private Store managedStore;
}