package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.SubCategoryDTO;
import com.analyfy.analify.Entity.SubCategory;


@Component
@Mapper(componentModel="spring")
public interface SubCategoryMapper extends BaseMapper<SubCategoryDTO, SubCategory>{
    
}
