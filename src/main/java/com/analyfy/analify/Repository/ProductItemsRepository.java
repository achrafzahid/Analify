package com.analyfy.analify.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.Inventory;

public interface ProductItemsRepository extends JpaRepository<Inventory, Long> {
    // Analytics: Get inventory for a specific store
    List<Inventory> findByStoreStoreId(Long storeId);
    
    // Analytics: Find low stock items across all stores
    List<Inventory> findByQuantityLessThan(int threshold);
}