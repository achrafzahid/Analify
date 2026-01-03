package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.CaissierDTO;
import com.analyfy.analify.Entity.Caissier;

@Mapper(componentModel = "spring")
public interface CaissierMapper extends BaseMapper<CaissierDTO, Caissier> {
    @Override
    @Mapping(source = "store.storeId", target = "storeId")
    @Mapping(source = "store.city.name", target = "storeName")
    CaissierDTO toDto(Caissier entity);
}