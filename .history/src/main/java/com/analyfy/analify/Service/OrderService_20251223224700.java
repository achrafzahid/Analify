package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.CreateOrderRequest;
import com.analyfy.analify.DTO.OrderDTO;
import com.analyfy.analify.DTO.OrderItemRequest;
import com.analyfy.analify.Entity.Caissier;
import com.analyfy.analify.Entity.Inventory;
import com.analyfy.analify.Entity.Order;
import com.analyfy.analify.Entity.OrderItems;
import com.analyfy.analify.Entity.Product;
import com.analyfy.analify.Entity.Store;
import com.analyfy.analify.Mapper.OrderMapper;
import com.analyfy.analify.Repository.CaissierRepository;
//import com.analyfy.analify.Repository.InventoryRepository;
import com.analyfy.analify.Repository.OrderRepository;
import com.analyfy.analify.Repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CaissierRepository caissierRepository;
    private final ProductRepository productRepository;
    //private final InventoryRepository inventoryRepository;
    private final OrderMapper orderMapper;

    /**
     * Créer une nouvelle commande
     */
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 1. Valider et récupérer le caissier
        Caissier caissier = caissierRepository.findById(request.getCashierId())
            .orElseThrow(() -> new RuntimeException("Caissier non trouvé avec l'ID: " + request.getCashierId()));

        // 2. Vérifier que le caissier a un magasin assigné
        Store store = caissier.getStore();
        if (store == null) {
            throw new RuntimeException("Le caissier n'est assigné à aucun magasin");
        }

        // 3. Créer l'entité Order
        Order order = new Order();
        order.setOrderDate(LocalDate.now());
        order.setShipDate(LocalDate.now()); // Peut être modifié selon la logique métier
        order.setCaissier(caissier);

        // 4. Créer les OrderItems
        List<OrderItems> orderItemsList = new ArrayList<>();
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            // Récupérer le produit
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + itemRequest.getProductId()));

            // Vérifier le stock disponible dans le magasin du caissier
            Inventory inventory = PRepository.findByStoreStoreIdAndProductProductId(
                store.getStoreId(), 
                product.getProductId()
            );

            if (inventory == null) {
                throw new RuntimeException("Produit '" + product.getProductName() + "' non disponible dans ce magasin");
            }

            if (inventory.getQuantite() < itemRequest.getQuantity()) {
                throw new RuntimeException("Stock insuffisant pour le produit '" + product.getProductName() + 
                    "'. Disponible: " + inventory.getQuantite() + ", Demandé: " + itemRequest.getQuantity());
            }

            // Créer l'OrderItem
            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(inventory.getSellingPrice()); // Prix de vente actuel du magasin
            orderItem.setDiscount(itemRequest.getDiscount() != null ? itemRequest.getDiscount() : 0.0);

            orderItemsList.add(orderItem);

            // Mettre à jour le stock
            inventory.setQuantite(inventory.getQuantite() - itemRequest.getQuantity());
            inventoryRepository.save(inventory);
        }

        // 5. Associer les items à la commande
        order.setItems(orderItemsList);

        // 6. Sauvegarder la commande (cascade sauvegarde les items)
        Order savedOrder = orderRepository.save(order);

        // 7. Convertir en DTO et retourner
        return orderMapper.toDto(savedOrder);
    }

    /**
     * Récupérer une commande par ID
     */
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'ID: " + orderId));
        return orderMapper.toDto(order);
    }

    /**
     * Récupérer toutes les commandes
     */
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
            .map(orderMapper::toDto)
            .toList();
    }

    /**
     * Récupérer les commandes par caissier
     */
    public List<OrderDTO> getOrdersByCashier(Long cashierId) {
        List<Order> orders = orderRepository.findByCaissierUserId(cashierId);
        return orders.stream()
            .map(orderMapper::toDto)
            .toList();
    }

    /**
     * Récupérer les commandes par magasin
     */
    public List<OrderDTO> getOrdersByStore(Long storeId) {
        List<Order> orders = orderRepository.findByCaissierStoreStoreId(storeId);
        return orders.stream()
            .map(orderMapper::toDto)
            .toList();
    }

    /**
     * Supprimer une commande (avec gestion du stock)
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'ID: " + orderId));

        Store store = order.getCaissier().getStore();

        // Restaurer le stock pour chaque item
        for (OrderItems item : order.getItems()) {
            Inventory inventory = inventoryRepository.findByStoreStoreIdAndProductProductId(
                store.getStoreId(),
                item.getProduct().getProductId()
            );

            if (inventory != null) {
                inventory.setQuantite(inventory.getQuantite() + item.getQuantity());
                inventoryRepository.save(inventory);
            }
        }

        orderRepository.delete(order);
    }

    /**
     * Mettre à jour la date d'expédition
     */
    @Transactional
    public OrderDTO updateShipDate(Long orderId, LocalDate shipDate) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'ID: " + orderId));
        
        order.setShipDate(shipDate);
        Order updatedOrder = orderRepository.save(order);
        
        return orderMapper.toDto(updatedOrder);
    }
}