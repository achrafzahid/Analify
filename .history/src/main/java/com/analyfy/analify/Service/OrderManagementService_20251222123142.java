package com.analyfy.analify.Service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import com.analyfy.analify.DTO.OrderDTO;
import com.analyfy.analify.Entity.Store;
import com.analyfy.analify.Entity.OrderItems;
import com.analyfy.analify.Entity.Order;
import com.analyfy.analify.Entity.Inventory;
import com.analyfy.analify.DTO.OrderItemDTO;
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
        StoreRepository storeRepository,
        OrderItemsRepository orderItemRepository,
        ProductItemsRepository productItemRepository
    ){
        this.orderItemRepository=orderItemRepository;
        this.orderRepository=orderRepository;
        this.storeRepository=storeRepository;
        this.productItemRepository=productItemRepository;
    }

    public OrderDTO saveOrder(OrderDTO orderDTO){

         // we need price, quantity, discount, caissier

         // 1. Charger le store
    Store store = storeRepository.findById(orderDTO.getStoreId())
            .orElseThrow(() -> new RuntimeException("Store not found"));

    // 2. Créer la commande
    Order order = new Order();
    order.setStore(store);
    order.setOrderDate(LocalDate.now());

    // Si tu as une règle métier pour shipDate
    order.setShipDate(LocalDate.now().plusDays(2));

    // 3. Sauvegarder la commande pour générer l'ID
    Order savedOrder = orderRepository.save(order);

    double totalAmount = 0.0;
    int totalItems = 0;

    // 4. Traiter les items
    for (OrderItemDTO itemDTO : orderDTO.getItems()) {

        Inventory productItem = productItemRepository
                .findById(itemDTO.getProductItemId())
                .orElseThrow(() -> new RuntimeException("Product item not found"));

        OrderItem orderItem = new ();
        orderItem.setOrder(savedOrder);
        orderItem.setProductItem(productItem);
        orderItem.setQuantity(itemDTO.getQuantity());
        orderItem.setPrice(itemDTO.getPrice());
        orderItem.setDiscount(itemDTO.getDiscount());

        double subTotal =
                (itemDTO.getPrice() * itemDTO.getQuantity()) - itemDTO.getDiscount();

        totalAmount += subTotal;
        totalItems += itemDTO.getQuantity();

        orderItemRepository.save(orderItem);
    }

    // 5. Mettre à jour les totaux
    savedOrder.setTotalAmount(totalAmount);
    savedOrder.setTotalItems(totalItems);
    orderRepository.save(savedOrder);

    // 6. Compléter le DTO en sortie
    orderDTO.setOrderId(savedOrder.getId());
    orderDTO.setOrderDate(savedOrder.getOrderDate());
    orderDTO.setShipDate(savedOrder.getShipDate());
    orderDTO.setStoreName(store.getName());
    orderDTO.setTotalAmount(totalAmount);
    orderDTO.setTotalItems(totalItems);


        return orderDTO;
    }
    
    
}
