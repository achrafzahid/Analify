package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;

import com.analyfy.analify.DTO.CategoryDTO;
import com.analyfy.analify.Entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper extends BaseMapper<CategoryDTO, Category> {}