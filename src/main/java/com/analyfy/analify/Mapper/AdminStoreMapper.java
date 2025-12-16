package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.adminStoreDTO;

@Component
@Mapper(componentModel="spring")
public interface AdminStoreMapper extends BaseMapper<adminStoreDTO, adminStoreDTO>{
    
}
