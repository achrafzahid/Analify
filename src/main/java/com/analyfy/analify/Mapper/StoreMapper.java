package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.StoreDTO;
import com.analyfy.analify.Entity.Store;


@Component
@Mapper(componentModel="spring")
public interface StoreMapper extends BaseMapper<StoreDTO, Store>{
    
}
