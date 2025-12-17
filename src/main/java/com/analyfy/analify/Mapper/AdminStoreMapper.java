package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.AdminStoreDTO;
import com.analyfy.analify.Entity.AdminStore;

@Mapper(componentModel = "spring")
public interface AdminStoreMapper extends BaseMapper<AdminStoreDTO, AdminStore> {
    @Override
    @Mapping(source = "managedStore.storeId", target = "managedStoreId")
    @Mapping(source = "managedStore.city.name", target = "managedStoreName")
    AdminStoreDTO toDto(AdminStore entity);
}