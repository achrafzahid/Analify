package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.analyfy.analify.DTO.AdminStoreDTO;
import com.analyfy.analify.DTO.CaissierDTO;
import com.analyfy.analify.DTO.EmployeeCreateDTO;
import com.analyfy.analify.DTO.EmployeeResponseDTO;
import com.analyfy.analify.DTO.UserDTO;
import com.analyfy.analify.Entity.AdminG;
import com.analyfy.analify.Entity.AdminStore;
import com.analyfy.analify.Entity.Caissier;
import com.analyfy.analify.Entity.Investor;
import com.analyfy.analify.Entity.User;
import com.analyfy.analify.Enum.UserRole;


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
    @Mapping(source = "store.storeId", target = "storeId")
    AdminStoreDTO toAdminStoreDto(AdminStore adminStore);
    
    AdminStore toAdminStoreEntity(AdminStoreDTO dto);

    // =========================================================
    // NEW METHODS FOR EMPLOYEE MANAGEMENT MODULE
    // =========================================================

    /**
     * 1. UPDATE ENTITY FROM DTO
     * Updates common fields (Name, Mail, DOB) from the CreateDTO to the Entity.
     * We use @MappingTarget to update the existing object.
     */
    @Mapping(target = "userId", ignore = true)   // ID is never updated from DTO
    @Mapping(target = "password", ignore = true) // Password handled by Service
    void updateEntity(@MappingTarget User user, EmployeeCreateDTO dto);

    /**
     * 2. UNIFIED RESPONSE MAPPING (The "Bridge")
     * MapStruct cannot easily handle the "instanceof" logic automatically.
     * We use a 'default' method to write custom Java logic here.
     */
    default EmployeeResponseDTO toEmployeeResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        EmployeeResponseDTO dto = new EmployeeResponseDTO();

        // Map Common Fields
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setMail(user.getMail());
        dto.setDateOfBirth(user.getDateOfBirth());

        // Dynamic Mapping based on Subclass
        if (user instanceof AdminG) {
            dto.setRole(UserRole.ADMIN_G);
        } 
        else if (user instanceof AdminStore) {
            AdminStore as = (AdminStore) user;
            dto.setRole(UserRole.ADMIN_STORE);
            
            // Map Salary & Date
            dto.setSalary(as.getSalary());
            dto.setDateStarted(as.getDateStarted());

            // Map Store ID (using your specific Entity structure)
            if (as.getStore() != null) {
                dto.setStoreId(as.getStore().getStoreId());
            }
        } 
        else if (user instanceof Caissier) {
            Caissier c = (Caissier) user;
            dto.setRole(UserRole.CAISSIER);
            
            // Map Salare -> Salary
            dto.setSalary(c.getSalary());
            dto.setDateStarted(c.getDateStarted());

            // Map Store ID
            if (c.getStore() != null) {
                dto.setStoreId(c.getStore().getStoreId());
            }
        } 
        else if (user instanceof Investor) {
            dto.setRole(UserRole.INVESTOR);
        }

        return dto;
    }
}