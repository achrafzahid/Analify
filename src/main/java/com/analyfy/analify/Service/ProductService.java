package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.*;
import com.analyfy.analify.Entity.*;
import com.analyfy.analify.Mapper.InventoryMapper;
import com.analyfy.analify.Mapper.ProductMapper;
import com.analyfy.analify.Repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final InvestorRepository investorRepository;
    private final ProductItemsRepository productItemsRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ProductMapper productMapper;
    private final InventoryMapper inventoryMapper;
    private final StoreRepository storeRepository;

    /**
     * Créer un nouveau produit
     */
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        // Vérifier si le produit existe déjà
        if (productRepository.existsByProductNameIgnoreCase(request.getProductName())) {
            throw new RuntimeException("Un produit avec ce nom existe déjà: " + request.getProductName());
        }

        // Récupérer la sous-catégorie
        Subcategory subcategory = subcategoryRepository.findById(request.getSubId())
            .orElseThrow(() -> new RuntimeException("Sous-catégorie non trouvée avec l'ID: " + request.getSubId()));

        // Récupérer l'investisseur
        Investor investor = investorRepository.findById(request.getInvestorId())
            .orElseThrow(() -> new RuntimeException("Investisseur non trouvé avec l'ID: " + request.getInvestorId()));

        // Créer le produit
        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setSubcategory(subcategory);
        product.setId_inv(investor);

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    /**
     * Mettre à jour un produit
     */
    @Transactional
    public ProductDTO updateProduct(Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + productId));

        // Mettre à jour le nom si fourni
        if (request.getProductName() != null && !request.getProductName().isEmpty()) {
            // Vérifier si un autre produit a déjà ce nom
            if (!product.getProductName().equalsIgnoreCase(request.getProductName()) &&
                productRepository.existsByProductNameIgnoreCase(request.getProductName())) {
                throw new RuntimeException("Un autre produit avec ce nom existe déjà: " + request.getProductName());
            }
            product.setProductName(request.getProductName());
        }

        // Mettre à jour la sous-catégorie si fournie
        if (request.getSubId() != null) {
            Subcategory subcategory = subcategoryRepository.findById(request.getSubId())
                .orElseThrow(() -> new RuntimeException("Sous-catégorie non trouvée avec l'ID: " + request.getSubId()));
            product.setSubcategory(subcategory);
        }

        // Mettre à jour l'investisseur si fourni
        if (request.getInvestorId() != null) {
            Investor investor = investorRepository.findById(request.getInvestorId())
                .orElseThrow(() -> new RuntimeException("Investisseur non trouvé avec l'ID: " + request.getInvestorId()));
            product.setId_inv(investor);
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }

    /**
     * Supprimer un produit
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + productId));

        // Vérifier si le produit a des commandes associées
        // (Optionnel: vous pouvez choisir de bloquer la suppression ou de la permettre)
        
        productRepository.delete(product);
    }

    /**
     * Récupérer tous les produits
     */
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
            .map(productMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Récupérer un produit par ID
     */
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + productId));
        return productMapper.toDto(product);
    }

    /**
     * Récupérer les produits par catégorie
     */
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findBySubcategoryCategoryCategoryId(categoryId);
        return products.stream()
            .map(productMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Récupérer les produits par sous-catégorie
     */
    public List<ProductDTO> getProductsBySubcategory(Long subId) {
        List<Product> products = productRepository.findBySubcategorySubId(subId);
        return products.stream()
            .map(productMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Rechercher des produits par nom
     */
    public List<ProductDTO> searchProducts(String query) {
        List<Product> products = productRepository.findByProductNameContainingIgnoreCase(query);
        return products.stream()
            .map(productMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Mettre à jour le stock d'un produit dans un magasin
     */
    @Transactional
    public InventoryDTO updateProductStock(Long productId, UpdateStockRequest request) {
        // Vérifier que le produit existe
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + productId));

        // Chercher ou créer l'inventaire
        Inventory inventory = productItemsRepository
            .findByStoreStoreIdAndProductProductId(request.getStoreId(), productId)
            .orElseGet(() -> {
                Inventory newInventory = new Inventory();
                newInventory.setProduct(product);
                
                // Récupérer le magasin
                Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Magasin non trouvé avec l'ID: " + request.getStoreId()));
                newInventory.setStore(store);
                newInventory.setQuantity(0);
                return newInventory;
            });

        // Mettre à jour la quantité
        inventory.setQuantity(request.getQuantity());
        Inventory savedInventory = productItemsRepository.save(inventory);

        return inventoryMapper.toDto(savedInventory);
    }

    /**
     * Récupérer le stock d'un produit dans tous les magasins
     */
    public List<InventoryDTO> getProductStock(Long productId) {
        List<Inventory> inventories = productItemsRepository.findByProductProductId(productId);
        return inventories.stream()
            .map(inventoryMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Récupérer les produits les plus vendus
     */
    public List<TopSellingProductDTO> getTopSellingProducts() {
        List<Object[]> results = orderItemsRepository.findTopSellingProducts();
        
        List<TopSellingProductDTO> topProducts = new ArrayList<>();
        for (Object[] result : results) {
            TopSellingProductDTO dto = new TopSellingProductDTO();
            dto.setProductId((Long) result[0]);
            dto.setProductName((String) result[1]);
            dto.setCategoryName((String) result[2]);
            dto.setTotalQuantitySold((Long) result[3]);
            dto.setTotalRevenue((Double) result[4]);
            topProducts.add(dto);
        }
        
        return topProducts;
    }

    /**
     * Récupérer le chiffre d'affaires d'un produit
     */
    public ProductRevenueDTO getProductRevenue(Long productId) {
        // Vérifier que le produit existe
        productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + productId));

        Object[] result = orderItemsRepository.findProductRevenue(productId);
        
        if (result == null) {
            // Aucune vente pour ce produit
            Product product = productRepository.findById(productId).get();
            return new ProductRevenueDTO(productId, product.getProductName(), 0.0, 0L);
        }
        
        return new ProductRevenueDTO(
            (Long) result[0],
            (String) result[1],
            (Double) result[2],
            (Long) result[3]
        );
    }

    /**
     * Calculer la valeur totale de l'inventaire par produit
     */
    public List<InventoryValueDTO> getInventoryValue() {
        List<Object[]> results = productItemsRepository.findInventoryValues();
        
        List<InventoryValueDTO> inventoryValues = new ArrayList<>();
        for (Object[] result : results) {
            InventoryValueDTO dto = new InventoryValueDTO();
            dto.setProductId((Long) result[0]);
            dto.setProductName((String) result[1]);
            dto.setTotalQuantity(((Number) result[2]).intValue());
            
            // Note: Vous devrez ajuster ce calcul selon votre logique métier
            // Par exemple: prix moyen * quantité
            dto.setEstimatedValue(0.0); // À calculer selon vos besoins
            
            inventoryValues.add(dto);
        }
        
        return inventoryValues;
    }
}