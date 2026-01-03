package com.analyfy.analify.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.AdminStore;

public interface AdminStoreRepository extends JpaRepository<AdminStore, Long> {

    List<AdminStore> findByStore_StoreId(Long storeId);
}