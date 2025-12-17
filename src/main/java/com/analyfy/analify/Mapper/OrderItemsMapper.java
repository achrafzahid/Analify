package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.OrderItemDTO;
import com.analyfy.analify.Entity.OrderItems;


@Mapper(componentModel = "spring")
public interface OrderItemsMapper extends BaseMapper<OrderItemDTO, OrderItems> {
    @Override
    @Mapping(source = "product.productId", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "product.subcategory.category.categoryName", target = "categoryName")
    @Mapping(target = "lineTotal", expression = "java((entity.getPrice() - (entity.getDiscount() != null ? entity.getDiscount() : 0)) * entity.getQuantity())")
    OrderItemDTO toDto(OrderItems entity);
}