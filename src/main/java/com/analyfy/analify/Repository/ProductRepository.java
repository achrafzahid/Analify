package com.analyfy.analify.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.Product;


public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySubcategorySubId(Long subId);
}