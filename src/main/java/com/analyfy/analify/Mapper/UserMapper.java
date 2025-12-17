package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.AdminStoreDTO;
import com.analyfy.analify.DTO.CaissierDTO;
import com.analyfy.analify.DTO.UserDTO;
import com.analyfy.analify.Entity.AdminStore;
import com.analyfy.analify.Entity.Caissier;
import com.analyfy.analify.Entity.User;


@Mapper(componentModel = "spring")
public interface UserMapper {
    // Basic User Mapping
    UserDTO toDto(User user);

    // Caissier Mapping
    @Mapping(source = "store.storeId", target = "storeId")
    @Mapping(source = "store.city.name", target = "storeName") // Example: Use City or Store ID as name if name is missing
    CaissierDTO toCaissierDto(Caissier caissier);

    Caissier toCaissierEntity(CaissierDTO dto);

    // AdminStore Mapping
    @Mapping(source = "managedStore.storeId", target = "managedStoreId")
    AdminStoreDTO toAdminStoreDto(AdminStore adminStore);
    
    AdminStore toAdminStoreEntity(AdminStoreDTO dto);
}