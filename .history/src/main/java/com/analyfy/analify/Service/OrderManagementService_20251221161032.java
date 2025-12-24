package com.analyfy.analify.Service;

import org.springframework.stereotype.Service;
import com.analyfy.analify.DTO.OrderDTO;
import com.analyfy.analify.Repository.OrderRepository;
import com.analyfy.analify.Repository.ProductItemsRepository;
import com.analyfy.analify.Repository.StoreRepository;
import com.analyfy.analify.Repository.OrderItemsRepository;

@Service
public class OrderManagementService {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final OrderItemsRepository orderItemRepository;
    private final ProductItemsRepository productItemRepository;

      

    public OrderManagementService(
        OrderRepository orderRepository,
        
    )
    
    
}
