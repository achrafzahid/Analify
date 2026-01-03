package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.InventoryDTO;
import com.analyfy.analify.Entity.Inventory;

@Mapper(componentModel = "spring")
public interface InventoryMapper extends BaseMapper<InventoryDTO, Inventory> {
    @Override
    @Mapping(source = "store.storeId", target = "storeId")
    @Mapping(source = "store.city.name", target = "storeName")
    @Mapping(source = "product.productId", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "product.subcategory.category.categoryName", target = "categoryName")
    @Mapping(target = "status", expression = "java(entity.getQuantity() < 10 ? \"Low Stock\" : \"In Stock\")")
    InventoryDTO toDto(Inventory entity);
}