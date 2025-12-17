package com.analyfy.analify.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.City;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByStateStateId(Long stateId);
}