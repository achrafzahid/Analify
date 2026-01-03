package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;

import com.analyfy.analify.DTO.RegionDTO;
import com.analyfy.analify.Entity.Region;

@Mapper(componentModel = "spring")
public interface RegionMapper extends BaseMapper<RegionDTO, Region> {}