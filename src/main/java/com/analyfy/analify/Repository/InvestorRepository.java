package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.Investor;

public interface InvestorRepository extends JpaRepository<Investor, Long> {}