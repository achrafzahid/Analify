package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.RegionDTO;
import com.analyfy.analify.Entity.Region;

@Component
@Mapper(componentModel="spring")
public interface RegionMapper extends BaseMapper<RegionDTO, Region>{
    
}
