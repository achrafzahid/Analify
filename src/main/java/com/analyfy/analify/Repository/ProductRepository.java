package com.analyfy.analify.Repository;

import com.analyfy.analify.Entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. Top Selling Products (Universal)
    @Query("SELECT p.productName, " + 
           "SUM((oi.price - (oi.price * oi.discount)) * oi.quantity), " +
           "c.categoryName " + 
           "FROM Order o JOIN o.items oi JOIN oi.product p JOIN p.subcategory sub JOIN sub.category c " +    
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId) " + 
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "GROUP BY p.productId, p.productName, c.categoryName " +
           "ORDER BY 2 DESC")
    List<Object[]> findTopSellingProducts(@Param("start") LocalDate start, 
                                          @Param("end") LocalDate end,
                                          @Param("investorId") Long investorId,
                                          @Param("storeId") Long storeId,
                                          Pageable pageable);

    // 2. Stock Value (Universal)
    @Query("SELECT SUM(p.price * pi.quantity) " +
           "FROM Inventory pi JOIN pi.product p JOIN pi.store s " +
           "WHERE (:storeId IS NULL OR s.storeId = :storeId) " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId)") // 🆕 Added
    Double calculateTotalStockValue(@Param("storeId") Long storeId, 
                                    @Param("investorId") Long investorId);

    // 3. Category Revenue (Universal)
    @Query("SELECT c.categoryName, SUM((oi.price - (oi.price * oi.discount)) * oi.quantity) " +
           "FROM Order o JOIN o.items oi JOIN oi.product p JOIN p.subcategory sub JOIN sub.category c " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId) " + // 🆕 Added
           "GROUP BY c.categoryName")
    List<Object[]> findCategoryRevenueDistribution(@Param("start") LocalDate start, 
                                                   @Param("end") LocalDate end, 
                                                   @Param("storeId") Long storeId,
                                                   @Param("investorId") Long investorId);

    // 4. Product Variety
    @Query("SELECT c.categoryName, COUNT(p) " +
           "FROM Product p JOIN p.subcategory sub JOIN sub.category c " +
           "WHERE (:investorId IS NULL OR p.id_inv.userId = :investorId) " +
           "GROUP BY c.categoryName")
    List<Object[]> countProductsByCategory(@Param("investorId") Long investorId);

    // 5. Low Stock Counter (Universal)
    @Query("SELECT COUNT(pi) FROM Inventory pi JOIN pi.product p JOIN pi.store s " +
           "WHERE pi.quantity < :threshold " +
           "AND (:storeId IS NULL OR s.storeId = :storeId) " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId)") // 🆕 Added
    Long countLowStockItems(@Param("storeId") Long storeId, 
                            @Param("investorId") Long investorId, 
                            @Param("threshold") int threshold);

    // 6. Top Stores (Admin Only)
    @Query("SELECT c.name, SUM((oi.price - (oi.price * oi.discount)) * oi.quantity) " +
           "FROM Order o JOIN o.items oi JOIN o.caissier u JOIN u.store s JOIN s.city c " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "GROUP BY c.id, c.name ORDER BY 2 DESC")
    List<Object[]> findTopStores(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);

    // 7. Top Investors (Admin Only)
    @Query("SELECT u.userName, SUM((oi.price - (oi.price * oi.discount)) * oi.quantity) " +
           "FROM Order o JOIN o.items oi JOIN oi.product p JOIN p.id_inv u " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "GROUP BY u.userId, u.userName ORDER BY 2 DESC")
    List<Object[]> findTopInvestors(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);
}