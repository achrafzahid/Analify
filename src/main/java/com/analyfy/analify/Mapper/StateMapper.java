package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.StateDTO;
import com.analyfy.analify.Entity.State;

@Component
@Mapper(componentModel="spring")
public interface StateMapper extends BaseMapper<StateDTO, State>{
    
}
