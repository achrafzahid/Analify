package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.*;
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

    /**
     * POST /api/products - Créer un nouveau produit
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        try {
            ProductDTO product = productService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/products/{id} - Mettre à jour un produit
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody UpdateProductRequest request) {
        try {
            ProductDTO product = productService.updateProduct(id, request);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/products/{id} - Supprimer un produit
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/products - Récupérer tous les produits
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/{id} - Récupérer un produit par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        try {
            ProductDTO product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/products/category/{categoryId} - Récupérer les produits par catégorie
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/subcategory/{subId} - Récupérer les produits par sous-catégorie
     */
    @GetMapping("/subcategory/{subId}")
    public ResponseEntity<List<ProductDTO>> getProductsBySubcategory(@PathVariable Long subId) {
        List<ProductDTO> products = productService.getProductsBySubcategory(subId);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/search?query=laptop - Rechercher des produits
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String query) {
        List<ProductDTO> products = productService.searchProducts(query);
        return ResponseEntity.ok(products);
    }

    /**
     * PUT /api/products/{id}/stock - Mettre à jour le stock d'un produit
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<InventoryDTO> updateProductStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {
        try {
            InventoryDTO inventory = productService.updateProductStock(id, request);
            return ResponseEntity.ok(inventory);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/products/{id}/stock - Récupérer le stock d'un produit
     */
    @GetMapping("/{id}/stock")
    public ResponseEntity<List<InventoryDTO>> getProductStock(@PathVariable Long id) {
        List<InventoryDTO> stock = productService.getProductStock(id);
        return ResponseEntity.ok(stock);
    }

    /**
     * GET /api/products/analytics/top-selling - Produits les plus vendus
     */
    @GetMapping("/analytics/top-selling")
    public ResponseEntity<List<TopSellingProductDTO>> getTopSellingProducts() {
        List<TopSellingProductDTO> topProducts = productService.getTopSellingProducts();
        return ResponseEntity.ok(topProducts);
    }

    /**
     * GET /api/products/{id}/revenue - Chiffre d'affaires d'un produit
     */
    @GetMapping("/{id}/revenue")
    public ResponseEntity<ProductRevenueDTO> getProductRevenue(@PathVariable Long id) {
        try {
            ProductRevenueDTO revenue = productService.getProductRevenue(id);
            return ResponseEntity.ok(revenue);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/products/analytics/inventory-value - Valeur de l'inventaire
     */
    @GetMapping("/analytics/inventory-value")
    public ResponseEntity<List<InventoryValueDTO>> getInventoryValue() {
        List<InventoryValueDTO> inventoryValue = productService.getInventoryValue();
        return ResponseEntity.ok(inventoryValue);
    }
}
