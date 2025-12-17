package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.ProductDTO;
import com.analyfy.analify.Entity.Product;


@Mapper(componentModel = "spring")
public interface ProductMapper extends BaseMapper<ProductDTO, Product> {
    @Override
    @Mapping(source = "subcategory.subId", target = "subId")
    @Mapping(source = "subcategory.subName", target = "subName")
    @Mapping(source = "subcategory.category.categoryName", target = "categoryName")
    ProductDTO toDto(Product entity);
}