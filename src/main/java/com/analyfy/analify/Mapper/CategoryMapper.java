package com.analyfy.analify.Mapper;

import java.util.Locale;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.CategoryDTO;

@Component
@Mapper(componentModel="spring")
public interface CategoryMapper extends BaseMapper<CategoryDTO,Locale.Category>{
    
}
