package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.OrderItemsDTO;
import com.analyfy.analify.Entity.OrderItems;


@Component
@Mapper(componentModel="spring")
public interface OrderItemsMapper extends BaseMapper<OrderItemsDTO, OrderItems>{
    
}
