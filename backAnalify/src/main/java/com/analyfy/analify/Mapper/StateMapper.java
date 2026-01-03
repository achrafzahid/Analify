package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.StateDTO;
import com.analyfy.analify.Entity.State;

@Mapper(componentModel = "spring")
public interface StateMapper extends BaseMapper<StateDTO, State> {
    @Override
    @Mapping(source = "region.regionId", target = "regionId")
    @Mapping(source = "region.name", target = "regionName")
    StateDTO toDto(State entity);
}
