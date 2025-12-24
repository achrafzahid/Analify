package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.CreateOrderRequest;
import com.analyfy.analify.DTO.OrderDTO;
import com.analyfy.analify.Entity.Inventory;
import com.analyfy.analify.Service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders - Créer une nouvelle commande
     */
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            OrderDTO createdOrder = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/orders/{id} - Récupérer une commande par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/orders - Récupérer toutes les commandes
     */
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/cashier/{cashierId} - Récupérer les commandes d'un caissier
     */
    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByCashier(@PathVariable Long cashierId) {
        List<OrderDTO> orders = orderService.getOrdersByCashier(cashierId);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/store/{storeId} - Récupérer les commandes d'un magasin
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStore(@PathVariable Long storeId) {
        List<OrderDTO> orders = orderService.getOrdersByStore(storeId);
        return ResponseEntity.ok(orders);
    }

    /**
     * PATCH /api/orders/{id}/ship-date - Mettre à jour la date d'expédition
     */
    @PatchMapping("/{id}/ship-date")
    public ResponseEntity<OrderDTO> updateShipDate(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shipDate) {
        try {
            OrderDTO updatedOrder = orderService.updateShipDate(id, shipDate);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/orders/{id} - Supprimer une commande
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/orders/low-stock/store/{storeId} - Vérifier les stocks bas d'un magasin
     */
    @GetMapping("/low-stock/store/{storeId}")
    public ResponseEntity<List<Inventory>> checkLowStock(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "10") int threshold) {
        List<Inventory> lowStockItems = orderService.checkLowStock(storeId, threshold);
        return ResponseEntity.ok(lowStockItems);
    }

    /**
     * GET /api/orders/low-stock/global - Vérifier les stocks bas dans tous les magasins
     */
    @GetMapping("/low-stock/global")
    public ResponseEntity<List<Inventory>> checkLowStockGlobal(
            @RequestParam(defaultValue = "10") int threshold) {
        List<Inventory> lowStockItems = orderService.checkLowStockGlobal(threshold);
        return ResponseEntity.ok(lowStockItems);
    }
}