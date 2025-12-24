package com.analyfy.analify.Service;

import org.springframework.stereotype.Service;
import com.analyfy.analify.DTO.OrderDTO;
import com.analyfy.analify.Repository.OrderRepository;
import com.analyfy.analify.Repository.StoreRepository;
@Service
public class OrderManagementService {

   private final OrderRepository orderRepository;
    private final CassierRepository caissierRepository;
    private final StoreRepository storeRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository,
                        CashierRepository cashierRepository,
                        StoreRepository storeRepository,
                        OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.cashierRepository = cashierRepository;
        this.storeRepository = storeRepository;
        this.orderItemRepository = orderItemRepository;
    }
    
}
