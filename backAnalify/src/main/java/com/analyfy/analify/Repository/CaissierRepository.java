package com.analyfy.analify.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.Caissier;

public interface CaissierRepository extends JpaRepository<Caissier, Long> {

    List<Caissier> findByStore_StoreId(Long storeId);
}