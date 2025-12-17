package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.OrderItems;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {}