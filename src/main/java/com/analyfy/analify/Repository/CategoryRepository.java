package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {}