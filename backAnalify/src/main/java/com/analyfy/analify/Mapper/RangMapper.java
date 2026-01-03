package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.analyfy.analify.DTO.RangDTO;
import com.analyfy.analify.Entity.Rang;

@Mapper(componentModel = "spring")
public interface RangMapper extends BaseMapper<RangDTO, Rang> {
    
    @Override
    @Mapping(source = "category.categoryId", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(target = "totalFaces", expression = "java(entity.getFaces() != null ? entity.getFaces().size() : 0)")
    @Mapping(target = "availableSections", expression = "java(countAvailableSections(entity))")
    RangDTO toDto(Rang entity);
    
    default Integer countAvailableSections(Rang rang) {
        if (rang.getFaces() == null) return 0;
        return rang.getFaces().stream()
            .flatMap(face -> face.getSections().stream())
            .filter(section -> section.getStatus() != null && section.getStatus().startsWith("OPEN"))
            .toArray().length;
    }
}