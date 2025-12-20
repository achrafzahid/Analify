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
    
    private Double salary;

    // THE OWNER
    // This maps to the physical column 'store_id' in your 'admin_store' table.
    // 'unique = true' ensures one Admin cannot manage multiple Stores.
    @OneToOne
    @JoinColumn(name = "store_id", unique = true)
    private Store managedStore;

    // ==========================================
    // HELPER METHOD (Syncs both sides)
    // ==========================================
    public void setManagedStore(Store managedStore) {
        this.managedStore = managedStore;
        // If the other side doesn't know about me yet, tell it.
        if (managedStore != null && managedStore.getManager() != this) {
            managedStore.setManager(this);
        }
    }
}