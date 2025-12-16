package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.InvestorDTO;
import com.analyfy.analify.Entity.Investor;

@Component
@Mapper(componentModel="spring")
public interface InvestorMapper extends BaseMapper<InvestorDTO, Investor>{
    
}
