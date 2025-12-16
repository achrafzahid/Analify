package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.CaissierDTO;
import com.analyfy.analify.Entity.Caissier;

@Component
@Mapper(componentModel="spring")
public interface CaissierMapper extends BaseMapper<CaissierDTO, Caissier>{
    
}
