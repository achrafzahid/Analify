package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.SubcategoryDTO;
import com.analyfy.analify.Entity.Subcategory;


@Mapper(componentModel = "spring")
public interface SubcategoryMapper extends BaseMapper<SubcategoryDTO, Subcategory> {
    @Override
    @Mapping(source = "category.categoryId", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    SubcategoryDTO toDto(Subcategory entity);
}
