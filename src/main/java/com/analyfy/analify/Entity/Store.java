package com.analyfy.analify.Entity;

import java.util.ArrayList;
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
    @Column(name = "store_id")
    private Long storeId;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    // THE INVERSE OWNER (Manager)
    @OneToOne(mappedBy = "store") 
    private AdminStore manager;

    // The Link to Orders is now hidden inside here
    @OneToMany(mappedBy = "store")
    private List<Caissier> employees = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<Inventory> inventory = new ArrayList<>();

    // Removed direct Orders list

    // ==========================================
    // HELPER METHOD (Syncs Manager)
    // ==========================================
    public void setManager(AdminStore manager) {
        this.manager = manager;
        if (manager != null && manager.getStore() != this) {
            manager.setStore(this);
        }
    }
}