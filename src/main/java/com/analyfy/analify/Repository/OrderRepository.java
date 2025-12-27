package com.analyfy.analify.Repository;

import com.analyfy.analify.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. Revenue Time Series (Universal Filter)
    @Query("SELECT CAST(o.orderDate AS string), " +
           "SUM( (oi.price - (oi.price * oi.discount)) * oi.quantity ) " +
           "FROM Order o JOIN o.items oi JOIN oi.product p " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId) " + // 🆕 Added
           "AND (:productId IS NULL OR p.productId = :productId) " +
           "GROUP BY o.orderDate ORDER BY o.orderDate ASC")
    List<Object[]> findRevenueTimeSeries(@Param("start") LocalDate start, 
                                         @Param("end") LocalDate end, 
                                         @Param("storeId") Long storeId,
                                         @Param("investorId") Long investorId,
                                         @Param("productId") Long productId);

    // 2. Total Revenue
    @Query("SELECT SUM( (oi.price - (oi.price * oi.discount)) * oi.quantity ) " +
           "FROM Order o JOIN o.items oi JOIN oi.product p " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId)")
    Double calculateTotalRevenue(@Param("start") LocalDate start, 
                                 @Param("end") LocalDate end, 
                                 @Param("storeId") Long storeId,
                                 @Param("investorId") Long investorId);

    // 3. Count Total Orders (Distinct orders containing relevant items)
    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items oi JOIN oi.product p " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId)")
    Long countTotalOrders(@Param("start") LocalDate start, 
                          @Param("end") LocalDate end, 
                          @Param("storeId") Long storeId,
                          @Param("investorId") Long investorId);

    // 4. Count Products Sold
    @Query("SELECT SUM(oi.quantity) FROM Order o JOIN o.items oi JOIN oi.product p " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId)")
    Integer countTotalProductsSold(@Param("start") LocalDate start, 
                                   @Param("end") LocalDate end, 
                                   @Param("storeId") Long storeId,
                                   @Param("investorId") Long investorId);

    // 5. Stock Demand / Quantity Forecast
    @Query("SELECT CAST(o.orderDate AS string), SUM(oi.quantity) " +
           "FROM Order o JOIN o.items oi JOIN oi.product p " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId) " +
           "AND (:productId IS NULL OR p.productId = :productId) " +
           "GROUP BY o.orderDate ORDER BY o.orderDate ASC")
    List<Object[]> findStockDemandTimeSeries(@Param("start") LocalDate start, 
                                             @Param("end") LocalDate end, 
                                             @Param("storeId") Long storeId,
                                             @Param("investorId") Long investorId,
                                             @Param("productId") Long productId);

    // 6. Safe Date Fetcher (For Java Charts)
    @Query("SELECT DISTINCT o.orderDate FROM Order o JOIN o.items oi JOIN oi.product p " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId)")
    List<LocalDate> findAllOrderDates(@Param("start") LocalDate start, 
                                      @Param("end") LocalDate end, 
                                      @Param("storeId") Long storeId,
                                      @Param("investorId") Long investorId);

    // 7. Sales By Region
    @Query("SELECT r.name, SUM((oi.price - (oi.price * oi.discount)) * oi.quantity) " +
           "FROM Order o JOIN o.items oi JOIN oi.product p " +
           "JOIN o.caissier u JOIN u.store s JOIN s.city c JOIN c.state st JOIN st.region r " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR s.storeId = :storeId) " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId) " +
           "GROUP BY r.name")
    List<Object[]> findSalesByRegion(@Param("start") LocalDate start, 
                                     @Param("end") LocalDate end, 
                                     @Param("storeId") Long storeId,
                                     @Param("investorId") Long investorId);

    // 8. Sales by State
    @Query("SELECT st.name, SUM((oi.price - (oi.price * oi.discount)) * oi.quantity) " +
           "FROM Order o JOIN o.items oi JOIN oi.product p " +
           "JOIN o.caissier u JOIN u.store s JOIN s.city c JOIN c.state st " +
           "WHERE o.orderDate BETWEEN :start AND :end " +
           "AND (:storeId IS NULL OR s.storeId = :storeId) " +
           "AND (:investorId IS NULL OR p.id_inv.userId = :investorId) " +
           "GROUP BY st.name")
    List<Object[]> findSalesByState(@Param("start") LocalDate start, 
                                    @Param("end") LocalDate end, 
                                    @Param("storeId") Long storeId,
                                    @Param("investorId") Long investorId);
}