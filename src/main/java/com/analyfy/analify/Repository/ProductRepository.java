package com.analyfy.analify.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.analyfy.analify.DTO.Statistics.RankingItem;
import com.analyfy.analify.Entity.Product;


public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySubcategorySubId(Long subId);

    // Returns Object[] to avoid errors
@   Query("SELECT p.productName, " + 
           "SUM((oi.price - (oi.price * oi.discount)) * oi.quantity), " +
           "c.categoryName " + 
           "FROM Order o " +
           "JOIN o.items oi " +
           "JOIN oi.product p " +
           "JOIN p.subcategory sub " + 
           "JOIN sub.category c " +    
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId) " + // 🛑 Fixed variable name here
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "GROUP BY p.productId, p.productName, c.categoryName " +
           "ORDER BY 2 DESC")
    List<Object[]> findTopSellingProducts(@Param("start") LocalDate start, 
                                          @Param("end") LocalDate end,
                                          @Param("investorId") Long investorId,
                                          @Param("storeId") Long storeId,
                                          Pageable pageable);

    // Keep the stock value query
    // Note: Assuming 'ProductItem' is the class name for your product_items table
    @Query("SELECT SUM(p.price * pi.quantity) " +
           "FROM Inventory pi " +
           "JOIN pi.product p " +
           "JOIN pi.store s " +
           "WHERE (:storeId IS NULL OR s.storeId = :storeId)")
    Double calculateTotalStockValue(@Param("storeId") Long storeId);


    // 🆕 NEW: Category Distribution (Revenue by Category)
    @Query("SELECT c.categoryName, SUM((oi.price - (oi.price * oi.discount)) * oi.quantity) " +
           "FROM Order o JOIN o.items oi JOIN oi.product p JOIN p.subcategory sub JOIN sub.category c " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "GROUP BY c.categoryName")
    List<Object[]> findCategoryDistribution(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("storeId") Long storeId);

    // 🆕 NEW: Top Stores (Global Admin Only)
   @Query("SELECT c.name, " +  // Assuming City entity has a 'name' field. Change to 'c.cityName' if needed.
           "SUM((oi.price - (oi.price * oi.discount)) * oi.quantity) " +
           "FROM Order o " +
           "JOIN o.items oi " +
           "JOIN o.caissier u " +
           "JOIN u.store s " +
           "JOIN s.city c " +   // Join Store -> City
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "GROUP BY c.id, c.name " + // Group by City
           "ORDER BY 2 DESC")
    List<Object[]> findTopStores(@Param("start") LocalDate start, 
                                 @Param("end") LocalDate end, 
                                 Pageable pageable);

    // 🆕 NEW: Top Investors (Global Admin Only)
    @Query("SELECT u.userName, SUM((oi.price - (oi.price * oi.discount)) * oi.quantity) " +
           "FROM Order o JOIN o.items oi JOIN oi.product p JOIN p.id_inv u " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "GROUP BY u.userId, u.userName ORDER BY 2 DESC")
    List<Object[]> findTopInvestors(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);



}