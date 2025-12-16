package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.ProductItemsDTO;
import com.analyfy.analify.Entity.ProductItems;


@Component
@Mapper(componentModel="spring")
public interface ProductItemsMapper extends BaseMapper<ProductItemsDTO, ProductItems>{
    
}
