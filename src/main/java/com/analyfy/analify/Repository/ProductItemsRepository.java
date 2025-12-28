package com.analyfy.analify.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.analyfy.analify.Entity.Inventory;

public interface ProductItemsRepository extends JpaRepository<Inventory, Long> {
    // Analytics: Get inventory for a specific store
    List<Inventory> findByStoreStoreId(Long storeId);
    
    // Analytics: Find low stock items across all stores
    List<Inventory> findByQuantityLessThan(int threshold);


    Optional<Inventory> findByStoreStoreIdAndProductProductId(Long storeId, Long productId);

    List<Inventory> findByProductProductId(Long productId);

    @Query("SELECT i.product.productId, " +
           "i.product.productName, " +
           "SUM(i.quantity) " +
           "FROM Inventory i " +
           "GROUP BY i.product.productId, i.product.productName " +
           "ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findInventoryValues();

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.product.productId = :productId")
    Integer getTotalQuantityByProduct(@Param("productId") Long productId);


    @Query("SELECT i FROM Inventory i WHERE i.quantity = 0")
    List<Inventory> findOutOfStockItems();

    @Query("SELECT i FROM Inventory i " +
           "WHERE i.store.storeId = :storeId AND i.quantity < :threshold")
    List<Inventory> findLowStockByStore(@Param("storeId") Long storeId, @Param("threshold") Integer threshold);

    @Query("SELECT i.product.productId, " +
           "i.product.productName, " +
           "SUM(i.quantity), " +
           "i.store.storeId, " +
           "i.store.city.name " +
           "FROM Inventory i " +
           "GROUP BY i.product.productId, i.product.productName, i.store.storeId, i.store.city.name " +
           "ORDER BY i.product.productName")
    List<Object[]> findInventoryByProductAndStore();

    boolean existsByStoreStoreIdAndProductProductId(Long storeId, Long productId);

    @Query("SELECT COUNT(DISTINCT i.product.productId) FROM Inventory i WHERE i.store.storeId = :storeId")
    Long countDistinctProductsByStore(@Param("storeId") Long storeId);



    
}