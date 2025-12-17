package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.CityDTO;
import com.analyfy.analify.Entity.City;

@Mapper(componentModel = "spring")
public interface CityMapper extends BaseMapper<CityDTO, City> {
    @Override
    @Mapping(source = "state.stateId", target = "stateId")
    @Mapping(source = "state.name", target = "stateName")
    @Mapping(source = "state.region.name", target = "regionName") // Flattened for easy analytics
    CityDTO toDto(City entity);
}