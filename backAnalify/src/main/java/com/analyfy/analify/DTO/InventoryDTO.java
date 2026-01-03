package com.analyfy.analify.DTO;
import lombok.Data;


@Data
public class InventoryDTO {
    private Long inventoryId; // The unique ID for the row
    
    // What is it?
    private Long productId;
    private String productName;
    private String categoryName;

    // Where is it?
    private Long storeId;
    private String storeName;

    // Status
    private Integer quantity;
    private String status; // e.g., "Low Stock", "In Stock" (Calculated in Mapper)
}
