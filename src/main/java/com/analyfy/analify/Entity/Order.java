package com.analyfy.analify.Entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "\"order\"")
@Getter @Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long orderId;

    @Column(name = "order_date")
    private LocalDate orderDate;
    
    @Column(name = "ship_date")
    private LocalDate shipDate;

    // Diagram shows "user_id" in Order table, and line points to Caissier
    @ManyToOne
    @JoinColumn(name = "user_id") 
    private Caissier caissier;

    // Diagram shows line between Store and Order
    // We assume there is an implicit store_id or it passes through Caissier
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItems> items;
}