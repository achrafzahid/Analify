package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.analyfy.analify.Entity.OrderItems;
import java.util.List;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {



    @Query("SELECT oi.product.productId, " +
           "oi.product.productName, " +
           "oi.product.subcategory.category.categoryName, " +
           "SUM(oi.quantity), " +
           "SUM((oi.price - oi.discount) * oi.quantity) " +
           "FROM OrderItems oi " +
           "GROUP BY oi.product.productId, oi.product.productName, oi.product.subcategory.category.categoryName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts();

    @Query("SELECT oi.product.productId, " +
           "oi.product.productName, " +
           "SUM((oi.price - oi.discount) * oi.quantity), " +
           "SUM(oi.quantity) " +
           "FROM OrderItems oi " +
           "WHERE oi.product.productId = :productId " +
           "GROUP BY oi.product.productId, oi.product.productName")
    Object[] findProductRevenue(@Param("productId") Long productId);

}