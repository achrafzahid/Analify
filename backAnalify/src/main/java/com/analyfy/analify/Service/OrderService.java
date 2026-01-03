package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.OrderDTO;
import com.analyfy.analify.DTO.StockOrder.CreateOrderRequest;
import com.analyfy.analify.DTO.StockOrder.OrderItemRequest;
import com.analyfy.analify.Entity.*;
import com.analyfy.analify.Enum.UserRole;
import com.analyfy.analify.Mapper.OrderMapper;
import com.analyfy.analify.Repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CaissierRepository caissierRepository;
    private final ProductRepository productRepository;
    private final ProductItemsRepository productItemsRepository; // Inventory
    private final StoreRepository storeRepository;
    private final AdminStoreRepository adminStoreRepository;
    private final InvestorRepository investorRepository;
    private final OrderMapper orderMapper;

    /**
     * Helper: Resolve Store ID for Admin_Store
     */
    private Long resolveStoreIdForAdmin(Long userId) {
        return adminStoreRepository.findById(userId)
                .map(AdminStore::getStore)
                .map(Store::getStoreId)
                .orElseThrow(() -> new RuntimeException("Store not found for Admin ID: " + userId));
    }


    /**
     * CREATE ORDER: Only Caissier
     */
    @Transactional
    public OrderDTO createOrder(Long userId, UserRole role, CreateOrderRequest request) {
        if (role != UserRole.CAISSIER) {
            throw new RuntimeException("Unauthorized: Only a Caissier can create orders.");
        }

        // 1. Verify Caissier Identity
        Caissier caissier = caissierRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Caissier profile not found for ID: " + userId));

        // 2. Verify Store Assignment
        Store store = caissier.getStore();
        if (store == null) {
            throw new RuntimeException("Caissier is not assigned to any store.");
        }

        // 3. Create Order Entity
        Order order = new Order();
        order.setOrderDate(LocalDate.now());
        order.setShipDate(LocalDate.now());
        order.setCaissier(caissier);

        // 4. Fetch Inventory for THIS Store
        List<Inventory> storeInventories = productItemsRepository.findByStoreStoreId(store.getStoreId());

        // 5. Process Items
        List<OrderItems> orderItemsList = new ArrayList<>();
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            // 🛑 CHANGED: Find by ID instead of Name
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemRequest.getProductId()));

            // Find matching inventory
            Inventory inventory = storeInventories.stream()
                .filter(inv -> inv.getProduct().getProductId().equals(product.getProductId()))
                .findFirst()
                .orElse(null);

            if (inventory == null) {
                throw new RuntimeException("Product ID " + product.getProductId() + " (" + product.getProductName() + ") not available in this store.");
            }

            if (inventory.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for '" + product.getProductName() + 
                    "'. Available: " + inventory.getQuantity() + ", Requested: " + itemRequest.getQuantity());
            }

            // Create OrderItem
            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice() != null ? product.getPrice() : 0.0);
            orderItem.setDiscount(itemRequest.getDiscount() != null ? itemRequest.getDiscount() : 0.0);

            orderItemsList.add(orderItem);

            // Update Stock
            inventory.setQuantity(inventory.getQuantity() - itemRequest.getQuantity());
            productItemsRepository.save(inventory);
        }

        order.setItems(orderItemsList);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    /**
     * GET ORDERS (Dashboard View)
     * - Caissier: Sees OWN orders.
     * - Admin_Store: Sees ALL orders in THEIR Store.
     * - Investor: Sees orders containing THEIR products.
     * - Admin_G: Sees ALL, with filters.
     */
    public List<OrderDTO> getOrdersDashboard(Long userId, UserRole role, 
                                             Long filterStoreId, Long filterRegionId, Long filterStateId, 
                                             Long filterCaissierId, Long filterProductId) {
        List<Order> orders;

        switch (role) {
            case CAISSIER:
                // - Caissier "effectuer" Order (1..*)
                orders = orderRepository.findByCaissierUserId(userId);
                break;

            case ADMIN_STORE:
                // - Admin_Store "gérer" Store -> Store "contient" Order (via Caissier)
                Long myStoreId = resolveStoreIdForAdmin(userId);
                // Can filter by product/caissier within their store
                orders = orderRepository.findAllByStoreWithFilters(myStoreId, filterCaissierId, filterProductId);
                break;

            case INVESTOR:
                // - Investor "fournir" Product -> Order contains Product
                orders = orderRepository.findOrdersByInvestorProduct(userId);
                break;

            case ADMIN_G:
                // - Full visibility + Geo Filters
                orders = orderRepository.findAllGlobalWithFilters(filterStoreId, filterRegionId, filterStateId, filterProductId);
                break;

            default:
                orders = Collections.emptyList();
        }

        return orders.stream().map(orderMapper::toDto).collect(Collectors.toList());
    }

    /**
     * GET ORDER BY ID (With Access Check)
     */
    public OrderDTO getOrderById(Long userId, UserRole role, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Authorization Logic
        boolean authorized = false;
        switch (role) {
            case CAISSIER:
                if (order.getCaissier().getUserId().equals(userId)) authorized = true;
                break;
            case ADMIN_STORE:
                Long myStoreId = resolveStoreIdForAdmin(userId);
                if (order.getCaissier().getStore().getStoreId().equals(myStoreId)) authorized = true;
                break;
            case INVESTOR:
                // Check if order contains ANY product owned by investor
                boolean ownsProduct = order.getItems().stream()
                    .anyMatch(item -> item.getProduct().getId_inv().getUserId().equals(userId));
                if (ownsProduct) authorized = true;
                break;
            case ADMIN_G:
                authorized = true;
                break;
        }

        if (!authorized) {
            throw new RuntimeException("Unauthorized to view this order.");
        }
        return orderMapper.toDto(order);
    }

    /**
     * UPDATE ORDER (Update Ship Date, etc.)
     * - Only Caissier (Creator) or Admin_G
     */
    @Transactional
    public OrderDTO updateOrder(Long userId, UserRole role, Long orderId, LocalDate shipDate) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (role == UserRole.CAISSIER) {
            if (!order.getCaissier().getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized: You can only update your own orders.");
            }
        } else if (role != UserRole.ADMIN_G) {
             // Admin_Store usually doesn't modify historical orders, but you can enable if needed.
             throw new RuntimeException("Unauthorized to update order.");
        }

        if (shipDate != null) order.setShipDate(shipDate);
        return orderMapper.toDto(orderRepository.save(order));
    }

    /**
     * DELETE ORDER (Restock Items)
     * - Only Caissier (Creator) or Admin_G
     */
    @Transactional
    public void deleteOrder(Long userId, UserRole role, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (role == UserRole.CAISSIER) {
            if (!order.getCaissier().getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized: You can only delete your own orders.");
            }
        } else if (role != UserRole.ADMIN_G) {
             throw new RuntimeException("Unauthorized to delete order.");
        }

        // Restore Stock
        Store store = order.getCaissier().getStore();
        List<Inventory> storeInventories = productItemsRepository.findByStoreStoreId(store.getStoreId());

        for (OrderItems item : order.getItems()) {
            Inventory inventory = storeInventories.stream()
                .filter(inv -> inv.getProduct().getProductId().equals(item.getProduct().getProductId()))
                .findFirst()
                .orElse(null);

            if (inventory != null) {
                inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
                productItemsRepository.save(inventory);
            }
        }

        orderRepository.delete(order);
    }

    /**
     * Helper: Calculate Total
     */
    public Double calculateOrderTotal(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        return order.getItems().stream()
            .mapToDouble(item -> (item.getPrice() - (item.getPrice() * item.getDiscount())) * item.getQuantity())
            .sum();
    }
}