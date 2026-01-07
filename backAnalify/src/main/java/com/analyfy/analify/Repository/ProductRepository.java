package com.analyfy.analify.Repository;

import com.analyfy.analify.Entity.Product;
import com.analyfy.analify.DTO.ProductDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ... (Keep existing simple methods: findByProductName, etc.) ...
    boolean existsByProductNameIgnoreCase(String productName);
    List<Product> findByProductNameContainingIgnoreCase(String query);
    List<Product> findBySubcategorySubId(Long subId);
    List<Product> findBySubcategoryCategoryCategoryId(Long categoryId);
    
    // Count products by investor
    @Query("SELECT COUNT(p) FROM Product p WHERE p.id_inv.userId = :investorId")
    Long countByInvestorUserId(@Param("investorId") Long investorId);

    // --- ENTITY FETCHING QUERIES (Keep these as they were) ---
    @Query("SELECT p FROM Product p WHERE p.id_inv.userId = :investorId")
    List<Product> findAllByInvestorId(@Param("investorId") Long investorId);

    @Query("SELECT DISTINCT p FROM Product p JOIN p.stocks i WHERE i.store.storeId = :storeId")
    List<Product> findAllByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.stocks i LEFT JOIN i.store s LEFT JOIN s.city c LEFT JOIN c.state st LEFT JOIN st.region r WHERE (:storeId IS NULL OR s.storeId = :storeId) AND (:stateId IS NULL OR st.stateId = :stateId) AND (:regionId IS NULL OR r.regionId = :regionId)")
    List<Product> findAllWithFilters(@Param("storeId") Long storeId, @Param("stateId") Long stateId, @Param("regionId") Long regionId);

    // --- FIXED DTO QUERIES (Now including subId) ---

    // 1. FOR INVESTOR DASHBOARD
    @Query("SELECT new com.analyfy.analify.DTO.ProductDTO(" +
           "p.productId, p.productName, p.price, " +
           "p.subcategory.category.categoryName, " +
           "p.subcategory.subId, " + // 🟢 Added subId
           "p.subcategory.subName, " +
           "p.id_inv.userId, p.id_inv.userName, " +
           "COALESCE(SUM(pi.quantity), 0)) " + 
           "FROM Product p " +
           "LEFT JOIN p.stocks pi " +
           "WHERE p.id_inv.userId = :investorId " +
           "GROUP BY p.productId, p.productName, p.price, p.subcategory.category.categoryName, p.subcategory.subId, p.subcategory.subName, p.id_inv.userId, p.id_inv.userName")
    List<ProductDTO> findAllByInvestorIdWithQuantity(@Param("investorId") Long investorId);

    // 2. FOR CAISSIER & ADMIN_STORE
    @Query("SELECT new com.analyfy.analify.DTO.ProductDTO(" +
           "p.productId, p.productName, p.price, " +
           "p.subcategory.category.categoryName, " +
           "p.subcategory.subId, " + // 🟢 Added subId
           "p.subcategory.subName, " +
           "p.id_inv.userId, p.id_inv.userName, " +
           "CAST(pi.quantity AS long)) " +
           "FROM Product p " +
           "JOIN p.stocks pi " +
           "WHERE pi.store.storeId = :storeId")
    List<ProductDTO> findAllByStoreIdWithQuantity(@Param("storeId") Long storeId);

    // 3. FOR ADMIN_G
    @Query("SELECT new com.analyfy.analify.DTO.ProductDTO(" +
           "p.productId, p.productName, p.price, " +
           "p.subcategory.category.categoryName, " +
           "p.subcategory.subId, " + // 🟢 Added subId
           "p.subcategory.subName, " +
           "p.id_inv.userId, p.id_inv.userName, " +
           "COALESCE(SUM(pi.quantity), 0)) " +
           "FROM Product p " +
           "LEFT JOIN p.stocks pi " +
           "LEFT JOIN pi.store s " +
           "LEFT JOIN s.city c " +
           "LEFT JOIN c.state st " +
           "LEFT JOIN st.region r " +
           "WHERE (:storeId IS NULL OR s.storeId = :storeId) " +
           "AND (:stateId IS NULL OR st.stateId = :stateId) " +
           "AND (:regionId IS NULL OR r.regionId = :regionId) " +
           "GROUP BY p.productId, p.productName, p.price, p.subcategory.category.categoryName, p.subcategory.subId, p.subcategory.subName, p.id_inv.userId, p.id_inv.userName")
    List<ProductDTO> findAllWithFiltersAndQuantity(@Param("storeId") Long storeId, 
                                                   @Param("stateId") Long stateId, 
                                                   @Param("regionId") Long regionId);

    // ... (Keep the rest of your statistics queries: findLowStockStoresForInvestor, findTopSellingProducts, etc.) ...
    @Query("SELECT p.productId, s.storeId, s.city.name, p.productName, pi.quantity FROM Inventory pi JOIN pi.product p JOIN pi.store s WHERE p.id_inv.userId = :investorId AND pi.quantity < 15")
    List<Object[]> findLowStockStoresForInvestor(@Param("investorId") Long investorId);

    @Query("SELECT p.productId, s.storeId, s.city.name, p.productName, pi.quantity FROM Inventory pi JOIN pi.product p JOIN pi.store s WHERE (:storeId IS NULL OR s.storeId = :storeId) AND pi.quantity < 15")
    List<Object[]> findLowStockByStore(@Param("storeId") Long storeId);

    @Query("SELECT p.productName, SUM((oi.price - (oi.price * oi.discount)) * oi.quantity), c.categoryName FROM Order o JOIN o.items oi JOIN oi.product p JOIN p.subcategory sub JOIN sub.category c WHERE o.orderDate BETWEEN :start AND :end AND (:investorId IS NULL OR p.id_inv.userId = :investorId) AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) GROUP BY p.productId, p.productName, c.categoryName ORDER BY 2 DESC")
    List<Object[]> findTopSellingProducts(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("investorId") Long investorId, @Param("storeId") Long storeId, Pageable pageable);

    @Query("SELECT SUM(p.price * pi.quantity) FROM Inventory pi JOIN pi.product p JOIN pi.store s WHERE (:storeId IS NULL OR s.storeId = :storeId) AND (:investorId IS NULL OR p.id_inv.userId = :investorId)")
    Double calculateTotalStockValue(@Param("storeId") Long storeId, @Param("investorId") Long investorId);

    @Query("SELECT c.categoryName, SUM((oi.price - (oi.price * oi.discount)) * oi.quantity) FROM Order o JOIN o.items oi JOIN oi.product p JOIN p.subcategory sub JOIN sub.category c WHERE o.orderDate BETWEEN :start AND :end AND (:storeId IS NULL OR o.caissier.store.storeId = :storeId) AND (:investorId IS NULL OR p.id_inv.userId = :investorId) GROUP BY c.categoryName")
    List<Object[]> findCategoryRevenueDistribution(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("storeId") Long storeId, @Param("investorId") Long investorId);

    @Query("SELECT c.categoryName, COUNT(p) FROM Product p JOIN p.subcategory sub JOIN sub.category c WHERE (:investorId IS NULL OR p.id_inv.userId = :investorId) GROUP BY c.categoryName")
    List<Object[]> countProductsByCategory(@Param("investorId") Long investorId);

    @Query("SELECT COUNT(pi) FROM Inventory pi JOIN pi.product p JOIN pi.store s WHERE pi.quantity < :threshold AND (:storeId IS NULL OR s.storeId = :storeId) AND (:investorId IS NULL OR p.id_inv.userId = :investorId)")
    Long countLowStockItems(@Param("storeId") Long storeId, @Param("investorId") Long investorId, @Param("threshold") int threshold);

    @Query("SELECT c.name, SUM((oi.price - (oi.price * oi.discount)) * oi.quantity) FROM Order o JOIN o.items oi JOIN o.caissier u JOIN u.store s JOIN s.city c WHERE o.orderDate BETWEEN :start AND :end GROUP BY c.id, c.name ORDER BY 2 DESC")
    List<Object[]> findTopStores(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);

    @Query("SELECT u.userName, SUM((oi.price - (oi.price * oi.discount)) * oi.quantity) FROM Order o JOIN o.items oi JOIN oi.product p JOIN p.id_inv u WHERE o.orderDate BETWEEN :start AND :end GROUP BY u.userId, u.userName ORDER BY 2 DESC")
    List<Object[]> findTopInvestors(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);
}