package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.CityDTO;
import com.analyfy.analify.Entity.City;

@Component
@Mapper(componentModel="spring")
public interface CityMapper extends BaseMapper<CityDTO,City>{
    
}
