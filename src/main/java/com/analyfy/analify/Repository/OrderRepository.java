package com.analyfy.analify.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.analyfy.analify.DTO.Statistics.TimeSeriesPoint;
import com.analyfy.analify.Entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Analytics: Find orders between dates
    List<Order> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Analytics: Find orders processed by a specific cashier
    List<Order> findByCaissierUserId(Long cashierId);

@Query("SELECT CAST(o.orderDate AS string), " +
           "SUM( (oi.price - (oi.price * oi.discount)) * oi.quantity ) " +
           "FROM Order o " +
           "JOIN o.items oi " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "AND (:productId IS NULL OR oi.product.productId = :productId) " +
           "GROUP BY o.orderDate " +
           "ORDER BY o.orderDate ASC")
    List<Object[]> findRevenueTimeSeries(@Param("start") LocalDate start, 
                                         @Param("end") LocalDate end, 
                                         @Param("storeId") Long storeId,
                                         @Param("productId") Long productId);

    // 2. Total Revenue Scalar
    @Query("SELECT SUM( (oi.price - (oi.price * oi.discount)) * oi.quantity ) " +
           "FROM Order o " +
           "JOIN o.items oi " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId)")
    Double calculateTotalRevenue(@Param("start") LocalDate start, 
                                 @Param("end") LocalDate end, 
                                 @Param("storeId") Long storeId);

    // 3. Count Orders
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId)")
    Long countTotalOrders(@Param("start") LocalDate start, 
                          @Param("end") LocalDate end, 
                          @Param("storeId") Long storeId);

    // 4. Count Products Sold
    @Query("SELECT SUM(oi.quantity) FROM Order o " +
           "JOIN o.items oi " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId)")
    Integer countTotalProductsSold(@Param("start") LocalDate start, 
                                   @Param("end") LocalDate end, 
                                   @Param("storeId") Long storeId);

    // 5. Stock Demand (For Admin - Global or Store)
    @Query("SELECT CAST(o.orderDate AS string), SUM(oi.quantity) " +
           "FROM Order o " +
           "JOIN o.items oi " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "GROUP BY o.orderDate " +
           "ORDER BY o.orderDate ASC")
    List<Object[]> findStockDemandTimeSeries(@Param("start") LocalDate start, 
                                             @Param("end") LocalDate end, 
                                             @Param("storeId") Long storeId);

    // 6. Investor Stock Demand (Specific or Combined)
    @Query("SELECT CAST(o.orderDate AS string), SUM(oi.quantity) " +
           "FROM Order o " +
           "JOIN o.items oi " +
           "JOIN oi.product p " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND p.id_inv.userId = :investorId " +
           "AND (:productId IS NULL OR p.productId = :productId) " +
           "GROUP BY o.orderDate " +
           "ORDER BY o.orderDate ASC")
    List<Object[]> findInvestorStockDemand(@Param("start") LocalDate start, 
                                           @Param("end") LocalDate end, 
                                           @Param("investorId") Long investorId,
                                           @Param("productId") Long productId);
}