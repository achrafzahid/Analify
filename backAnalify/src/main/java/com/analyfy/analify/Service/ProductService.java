package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.*; // Uses flat DTOs (not StockOrder.*)
import com.analyfy.analify.DTO.StockOrder.*; // Uses flat DTOs (not StockOrder.*)
import com.analyfy.analify.Entity.*;
import com.analyfy.analify.Enum.UserRole;
import com.analyfy.analify.Mapper.InventoryMapper;
import com.analyfy.analify.Mapper.ProductMapper;
import com.analyfy.analify.Repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final InvestorRepository investorRepository;
    private final ProductItemsRepository productItemsRepository;
    private final StoreRepository storeRepository;
    private final AdminStoreRepository adminStoreRepository;
    private final CaissierRepository caissierRepository;
    private final UserRepository userRepository;
    
    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;

    private Long resolveStoreIdForAdmin(Long userId) {
        return adminStoreRepository.findById(userId)
                .map(AdminStore::getStore)
                .map(Store::getStoreId)
                .orElseThrow(() -> new RuntimeException("Store not found for Admin ID: " + userId));
    }

    private Long resolveStoreIdForCaissier(Long userId) {
        return caissierRepository.findById(userId)
                .map(Caissier::getStore)
                .map(Store::getStoreId)
                .orElseThrow(() -> new RuntimeException("Store not found for Caissier ID: " + userId));
    }

    // --- DASHBOARD ---
    public List<ProductDTO> getProductsDashboard(Long userId, UserRole role, Long filterStoreId, Long filterStateId, Long filterRegionId) {
        switch (role) {
            case INVESTOR:
                return productRepository.findAllByInvestorIdWithQuantity(userId);
            case ADMIN_STORE:
                Long adminStoreId = resolveStoreIdForAdmin(userId);
                return productRepository.findAllByStoreIdWithQuantity(adminStoreId);
            case CAISSIER:
                Long caissierStoreId = resolveStoreIdForCaissier(userId);
                return productRepository.findAllByStoreIdWithQuantity(caissierStoreId);
            case ADMIN_G:
                return productRepository.findAllWithFiltersAndQuantity(filterStoreId, filterStateId, filterRegionId);
            default:
                return new ArrayList<>();
        }
    }

    // --- REPORTING ---
    public List<LowStockAlertDTO> getInvestorLowStockReport(Long userId, UserRole role, Long storeId) {
        List<Object[]> results;
        
        if (role == UserRole.INVESTOR) {
            // Investor sees only their products across all stores
            results = productRepository.findLowStockStoresForInvestor(userId);
        } else if (role == UserRole.ADMIN_STORE) {
            // Admin Store sees only their store - fetch store from user
            Long adminStoreId = userRepository.findById(userId)
                .filter(u -> u instanceof com.analyfy.analify.Entity.AdminStore)
                .map(u -> ((com.analyfy.analify.Entity.AdminStore) u).getStore().getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found for admin"));
            results = productRepository.findLowStockByStore(adminStoreId);
        } else if (role == UserRole.ADMIN_G) {
            // Admin G can filter by store or see all stores
            results = productRepository.findLowStockByStore(storeId);
        } else {
            throw new RuntimeException("Unauthorized");
        }
        
        List<LowStockAlertDTO> alerts = new ArrayList<>();
        for (Object[] row : results) {
            LowStockAlertDTO dto = new LowStockAlertDTO();
            dto.setProductId((Long) row[0]);
            dto.setStoreId((Long) row[1]);
            dto.setStoreCity((String) row[2]);
            dto.setProductName((String) row[3]);
            dto.setQuantity((Integer) row[4]);
            alerts.add(dto);
        }
        return alerts;
    }

    // --- CRUD ACTIONS (Fixing signatures here) ---
    @Transactional
    public ProductDTO createProduct(Long userId, UserRole role, CreateProductRequest request) {
        // 1. Authorization & Validation (Keep existing logic)
        if (role != UserRole.INVESTOR && role != UserRole.ADMIN_G) {
            throw new RuntimeException("Unauthorized: Only Investors or Global Admins can create products.");
        }
        if (productRepository.existsByProductNameIgnoreCase(request.getProductName())) {
            throw new RuntimeException("Product name already exists: " + request.getProductName());
        }

        Subcategory subcategory = subcategoryRepository.findById(request.getSubId())
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        // 2. Determine Owner (Keep existing logic)
        Investor owner;
        if (role == UserRole.INVESTOR) {
            owner = investorRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Investor profile not found"));
        } else {
            if (request.getInvestorId() == null) throw new RuntimeException("Admin must specify Investor ID");
            owner = investorRepository.findById(request.getInvestorId())
                    .orElseThrow(() -> new RuntimeException("Investor not found"));
        }

        // 3. Create & Save Product
        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setSubcategory(subcategory);
        product.setId_inv(owner);
        product.setPrice(request.getPrice()); // Set Price

        Product savedProduct = productRepository.save(product);

        // 🆕 4. AUTOMATIC INVENTORY CREATION FOR ALL STORES
        List<Store> allStores = storeRepository.findAll();
        List<Inventory> newInventories = new ArrayList<>();

        for (Store store : allStores) {
            Inventory inventory = new Inventory();
            inventory.setProduct(savedProduct);
            inventory.setStore(store);
            // Set initial quantity (defaults to 0 if null)
            inventory.setQuantity(request.getInitialQuantity() != null ? request.getInitialQuantity() : 0);
            
            newInventories.add(inventory);
        }

        // Batch save for performance
        productItemsRepository.saveAll(newInventories);

        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long userId, UserRole role, Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (role == UserRole.INVESTOR) {
            if (!product.getId_inv().getUserId().equals(userId)) throw new RuntimeException("Unauthorized");
            // Prevent category change
            if (request.getSubId() != null && !request.getSubId().equals(product.getSubcategory().getSubId())) {
                throw new RuntimeException("Investors cannot change category");
            }
        } else if (role != UserRole.ADMIN_G) {
            throw new RuntimeException("Unauthorized");
        }

        if (request.getProductName() != null) product.setProductName(request.getProductName());
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        
        if (role == UserRole.ADMIN_G) {
            if (request.getSubId() != null) product.setSubcategory(subcategoryRepository.findById(request.getSubId()).orElseThrow());
            if (request.getInvestorId() != null) product.setId_inv(investorRepository.findById(request.getInvestorId()).orElseThrow());
        }

        return productMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long userId, UserRole role, Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        if (role == UserRole.INVESTOR && !product.getId_inv().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        } else if (role != UserRole.ADMIN_G && role != UserRole.INVESTOR) {
            throw new RuntimeException("Unauthorized");
        }
        productRepository.delete(product);
    }

    @Transactional
    public InventoryDTO refillStock(Long userId, UserRole role, Long productId, UpdateStockRequest request) {
        Product product = productRepository.findById(productId).orElseThrow();
        if (role == UserRole.INVESTOR && !product.getId_inv().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        } else if (role != UserRole.ADMIN_G && role != UserRole.INVESTOR) {
            throw new RuntimeException("Unauthorized");
        }

        Inventory inventory = productItemsRepository
            .findByStoreStoreIdAndProductProductId(request.getStoreId(), productId)
            .orElseGet(() -> {
                Inventory newInv = new Inventory();
                newInv.setProduct(product);
                newInv.setStore(storeRepository.findById(request.getStoreId()).orElseThrow());
                newInv.setQuantity(0);
                return newInv;
            });

        // Add to existing quantity instead of replacing
        inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
        return inventoryMapper.toDto(productItemsRepository.save(inventory));
    }

    /**
     * Get suggested stock refill quantity for a product
     */
    public Integer getSuggestedStockQuantity(Long productId, Long storeId) {
        Product product = productRepository.findById(productId).orElseThrow();
        
        // Get current stock for this product at this store
        Integer currentStock = productItemsRepository
            .findByStoreStoreIdAndProductProductId(storeId, productId)
            .map(Inventory::getQuantity)
            .orElse(0);
        
        // Calculate recommended stock based on current level
        return calculateRecommendedStock(currentStock);
    }
    
    private int calculateRecommendedStock(int currentStock) {
        if (currentStock < 5) return 50;
        if (currentStock < 10) return 40;
        if (currentStock < 20) return 30;
        return 25;
    }

    // --- READ ONLY ---
    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id).map(productMapper::toDto).orElseThrow();
    }

    public List<ProductDTO> searchProducts(String query) {
        return productRepository.findByProductNameContainingIgnoreCase(query)
                .stream().map(productMapper::toDto).collect(Collectors.toList());
    }
}