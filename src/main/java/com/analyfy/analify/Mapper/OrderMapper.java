package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.OrderDTO;
import com.analyfy.analify.Entity.Order;

@Component
@Mapper(componentModel="spring")
public interface OrderMapper extends BaseMapper<OrderDTO, Order>{
    
}
