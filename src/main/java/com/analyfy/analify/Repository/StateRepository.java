package com.analyfy.analify.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.State;

public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findByRegionRegionId(Long regionId);
}