package com.analyfy.analify.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Analytics: Find orders between dates
    List<Order> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Analytics: Find orders processed by a specific cashier
    List<Order> findByCaissierUserId(Long cashierId);

    // Trouver les commandes par ID du magasin (via caissier)
    List<Order> findByCaissierStoreStoreId(Long storeId);
    
}