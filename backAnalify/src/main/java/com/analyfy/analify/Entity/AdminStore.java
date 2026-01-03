package com.analyfy.analify.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin_store")
@Getter @Setter
@PrimaryKeyJoinColumn(name = "user_id")
//@DiscriminatorValue("ADMIN_STORE")
public class AdminStore extends User {

    @Column(name = "date_started")
    private LocalDate dateStarted;
    
    private Double salary;

    // THE OWNER
    // This maps to the physical column 'store_id' in your 'admin_store' table.
    // 'unique = true' ensures one Admin cannot manage multiple Stores.
    @OneToOne
    @JoinColumn(name = "store_id", unique = true)
    private Store store;

    // ==========================================
    // HELPER METHOD (Syncs both sides)
    // ==========================================
    public void setStore(Store store) {
        this.store = store;
        // If the other side doesn't know about me yet, tell it.
        if (store != null && store.getManager() != this) {
            store.setManager(this);
        }
    }
}