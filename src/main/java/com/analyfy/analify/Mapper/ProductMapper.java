package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.ProductDTO;
import com.analyfy.analify.Entity.Product;


@Component
@Mapper(componentModel="spring")
public interface  ProductMapper extends BaseMapper<ProductDTO, Product> {
    
}
