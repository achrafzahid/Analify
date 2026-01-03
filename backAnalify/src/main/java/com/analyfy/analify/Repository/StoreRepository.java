package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
    // Analytics: Count stores by city
    long countByCityCityId(Long cityId);

   
}