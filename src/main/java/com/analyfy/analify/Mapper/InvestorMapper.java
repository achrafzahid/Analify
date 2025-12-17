package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;

import com.analyfy.analify.DTO.InvestorDTO;
import com.analyfy.analify.Entity.Investor;

@Mapper(componentModel = "spring")
public interface InvestorMapper extends BaseMapper<InvestorDTO, Investor> {}