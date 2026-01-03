package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.StoreDTO;
import com.analyfy.analify.Entity.Store;


@Mapper(componentModel = "spring")
public interface StoreMapper extends BaseMapper<StoreDTO, Store> {
    @Override
    @Mapping(source = "city.cityId", target = "cityId")
    @Mapping(source = "city.name", target = "cityName")
    @Mapping(source = "city.state.region.name", target = "regionName")
    @Mapping(source = "manager.userId", target = "managerId")
    @Mapping(source = "manager.userName", target = "managerName")
    @Mapping(target = "employeeCount", expression = "java(entity.getEmployees() != null ? entity.getEmployees().size() : 0)")
    StoreDTO toDto(Store entity);
}