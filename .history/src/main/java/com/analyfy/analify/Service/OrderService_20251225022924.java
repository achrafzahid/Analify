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
import com.analyfy.analify.Repository.ProductItemsRepository;
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
    private final ProductItemsRepository productItemsRepository;
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

        // 4. Récupérer tous les inventaires du magasin une seule fois (optimisation)
        List<Inventory> storeInventories = productItemsRepository.findByStoreStoreId(store.getStoreId());

        // 5. Créer les OrderItems
        List<OrderItems> orderItemsList = new ArrayList<>();
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            // Récupérer le produit
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + itemRequest.getProductId()));

            // Chercher l'inventaire correspondant dans la liste
            Inventory inventory = storeInventories.stream()
                .filter(inv -> inv.getProduct().getProductId().equals(product.getProductId()))
                .findFirst()
                .orElse(null);

            if (inventory == null) {
                throw new RuntimeException("Produit '" + product.getProductName() + "' non disponible dans ce magasin");
            }

            if (inventory.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Stock insuffisant pour le produit '" + product.getProductName() + 
                    "'. Disponible: " + inventory.getQuantity() + ", Demandé: " + itemRequest.getQuantity());
            }

            // Créer l'OrderItem
            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
           orderItem.setPrice(product.getSellingPrice()); // Prix de vente actuel du magasin
            orderItem.setDiscount(itemRequest.getDiscount() != null ? itemRequest.getDiscount() : 0.0);

            orderItemsList.add(orderItem);

            // Mettre à jour le stock
            inventory.setQuantity(inventory.getQuantity() - itemRequest.getQuantity());
            productItemsRepository.save(inventory);
        }

        // 6. Associer les items à la commande
        order.setItems(orderItemsList);

        // 7. Sauvegarder la commande (cascade sauvegarde les items)
        Order savedOrder = orderRepository.save(order);

        // 8. Convertir en DTO et retourner
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

        // Récupérer tous les inventaires du magasin
        List<Inventory> storeInventories = productItemsRepository.findByStoreStoreId(store.getStoreId());

        // Restaurer le stock pour chaque item
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

    /**
     * Vérifier les stocks bas dans un magasin
     */
    public List<Inventory> checkLowStock(Long storeId, int threshold) {
        List<Inventory> storeInventories = productItemsRepository.findByStoreStoreId(storeId);
        return storeInventories.stream()
            .filter(inv -> inv.getQuantity() < threshold)
            .toList();
    }

    /**
     * Vérifier les stocks bas dans tous les magasins
     */
    public List<Inventory> checkLowStockGlobal(int threshold) {
        return productItemsRepository.findByQuantityLessThan(threshold);
    }

    /**
 * Calculer le total d'une commande
 * @param orderId ID de la commande
 * @return Le montant total de la commande
 */
public Double calculateOrderTotal(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'ID: " + orderId));
    
    return order.getItems().stream()
        .mapToDouble(item -> (item.getPrice() - (item.getPrice() * item.getDiscount())) * item.getQuantity())
        .sum();
}

}