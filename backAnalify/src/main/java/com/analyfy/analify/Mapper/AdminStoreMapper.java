package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.AdminStoreDTO;
import com.analyfy.analify.Entity.AdminStore;

@Mapper(componentModel = "spring")
public interface AdminStoreMapper extends BaseMapper<AdminStoreDTO, AdminStore> {
    @Override
    @Mapping(source = "store.storeId", target = "storeId")
    @Mapping(source = "store.city.name", target = "storeName")
    AdminStoreDTO toDto(AdminStore entity);
}