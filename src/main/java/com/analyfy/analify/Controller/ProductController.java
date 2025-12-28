package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.*; // 🟢 Ensure this is the ONLY DTO import
import com.analyfy.analify.DTO.StockOrder.*; // 🟢 Ensure this is the ONLY DTO import
import com.analyfy.analify.Enum.UserRole;
import com.analyfy.analify.Service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProducts(
            @RequestHeader("X-Acting-User-Id") Long userId,
            @RequestHeader("X-Acting-User-Role") UserRole role,
            @RequestParam(required = false) Long filterStoreId,
            @RequestParam(required = false) Long filterStateId,
            @RequestParam(required = false) Long filterRegionId) {
        return ResponseEntity.ok(productService.getProductsDashboard(userId, role, filterStoreId, filterStateId, filterRegionId));
    }

    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestHeader("X-Acting-User-Id") Long userId,
            @RequestHeader("X-Acting-User-Role") UserRole role,
            @Valid @RequestBody CreateProductRequest request) { // 🟢 Uses standard DTO
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productService.createProduct(userId, role, request));
        } catch (RuntimeException e) {
            e.printStackTrace(); // 🖨️ Prints the real error to your console
            // 🟢 Returns the error message to Postman
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @RequestHeader("X-Acting-User-Id") Long userId,
            @RequestHeader("X-Acting-User-Role") UserRole role,
            @PathVariable Long id,
            @RequestBody UpdateProductRequest request) { // 🟢 Uses standard DTO
        try {
            return ResponseEntity.ok(productService.updateProduct(userId, role, id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader("X-Acting-User-Id") Long userId,
            @RequestHeader("X-Acting-User-Role") UserRole role,
            @PathVariable Long id) {
        try {
            productService.deleteProduct(userId, role, id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<InventoryDTO> updateProductStock(
            @RequestHeader("X-Acting-User-Id") Long userId,
            @RequestHeader("X-Acting-User-Role") UserRole role,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) { // 🟢 Uses standard DTO
        try {
            return ResponseEntity.ok(productService.refillStock(userId, role, id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/alerts/low-stock")
    public ResponseEntity<List<LowStockAlertDTO>> getLowStockAlerts(
            @RequestHeader("X-Acting-User-Id") Long userId,
            @RequestHeader("X-Acting-User-Role") UserRole role) {
        try {
            return ResponseEntity.ok(productService.getInvestorLowStockReport(userId, role));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String query) {
        return ResponseEntity.ok(productService.searchProducts(query));
    }
}