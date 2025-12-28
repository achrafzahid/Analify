package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.OrderDTO;
import com.analyfy.analify.DTO.StockOrder.CreateOrderRequest;
import com.analyfy.analify.Enum.UserRole;
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
     * CREATE ORDER
     * - Only Caissier
     */
    @PostMapping
    public ResponseEntity<?> createOrder( // 👈 Change return type to <?> to allow Error Map
            @RequestHeader("X-Acting-User-Id") Long userId,
            @RequestHeader("X-Acting-User-Role") UserRole role,
            @Valid @RequestBody CreateOrderRequest request) {
        try {
            OrderDTO createdOrder = orderService.createOrder(userId, role, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (RuntimeException e) {
            e.printStackTrace(); // 🖨️ Print stack trace to console
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage())); // 📤 Send message to Postman
        }
    }

    /**
     * GET ALL / DASHBOARD
     * - Filtered by Role (Caissier=Own, Store=Store, Investor=Product, Admin=Global)
     */
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrders(
            @RequestHeader("X-Acting-User-Id") Long userId,
            @RequestHeader("X-Acting-User-Role") UserRole role,
            @RequestParam(required = false) Long filterStoreId,
            @RequestParam(required = false) Long filterRegionId,
            @RequestParam(required = false) Long filterStateId,
            @RequestParam(required = false) Long filterCaissierId,
            @RequestParam(required = false) Long filterProductId) {
        
        List<OrderDTO> orders = orderService.getOrdersDashboard(
                userId, role, filterStoreId, filterRegionId, filterStateId, filterCaissierId, filterProductId);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET ORDER BY ID
     * - RBAC Check inside Service
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
            @RequestHeader("X-Acting-User-Id") Long userId,
            @RequestHeader("X-Acting-User-Role") UserRole role,
            @PathVariable Long id) {
        try {
            OrderDTO order = orderService.getOrderById(userId, role, id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * UPDATE ORDER (e.g., Ship Date)
     * - Only Creator (Caissier) or Admin_G
     */
    @PatchMapping("/{id}/ship-date")
    public ResponseEntity<OrderDTO> updateOrder(
            @RequestHeader("X-Acting-User-Id") Long userId,
            @RequestHeader("X-Acting-User-Role") UserRole role,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shipDate) {
        try {
            OrderDTO updatedOrder = orderService.updateOrder(userId, role, id, shipDate);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * DELETE ORDER
     * - Only Creator (Caissier) or Admin_G 
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @RequestHeader("X-Acting-User-Id") Long userId,
            @RequestHeader("X-Acting-User-Role") UserRole role,
            @PathVariable Long id) {
        try {
            orderService.deleteOrder(userId, role, id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * GET TOTAL
     */
    @GetMapping("/{id}/total")
    public ResponseEntity<Double> getOrderTotal(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.calculateOrderTotal(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}