package com.analyfy.analify.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.analyfy.analify.Entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find by subcategory
    List<Product> findBySubcategorySubId(Long subId);
    
    // Find by category
    List<Product> findBySubcategoryCategoryCategoryId(Long categoryId);
    
    // Search by name (exact, ignore case)
    Optional<Product> findByProductNameIgnoreCase(String productName);
    
    // Search by name (partial, ignore case)
    List<Product> findByProductNameContainingIgnoreCase(String query);
    
    // Check if product name already exists
    boolean existsByProductNameIgnoreCase(String productName);
}