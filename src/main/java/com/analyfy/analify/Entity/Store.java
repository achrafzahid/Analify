package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "store")
@Getter @Setter
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long storeId;

    // Connection to Location
    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    // Connection to Manager (User)
    @OneToOne
    @JoinColumn(name = "user_id") // Diagram shows store has user_id
    private AdminStore manager;

    // Connection to Employees
    @OneToMany(mappedBy = "store")
    private List<Caissier> employees;

    // Connection to Inventory
    @OneToMany(mappedBy = "store")
    private List<Inventory> inventory;

    // Connection to Orders (Diagram line exists between Store and Order)
    @OneToMany(mappedBy = "store")
    private List<Order> orders;
}