package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {}