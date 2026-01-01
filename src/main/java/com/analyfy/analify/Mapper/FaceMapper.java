package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.analyfy.analify.DTO.FaceDTO;
import com.analyfy.analify.Entity.Face;

@Mapper(componentModel = "spring")
public interface FaceMapper extends BaseMapper<FaceDTO, Face> {
    
    @Override
    @Mapping(source = "rang.rangId", target = "rangId")
    @Mapping(source = "rang.rangName", target = "rangName")
    @Mapping(source = "rang.category.categoryId", target = "categoryId")
    @Mapping(source = "rang.category.categoryName", target = "categoryName")
    @Mapping(target = "totalSections", expression = "java(entity.getSections() != null ? entity.getSections().size() : 0)")
    @Mapping(target = "openSections", expression = "java(countOpenSections(entity))")
    @Mapping(target = "closedSections", expression = "java(countClosedSections(entity))")
    FaceDTO toDto(Face entity);
    
    default Integer countOpenSections(Face face) {
        if (face.getSections() == null) return 0;
        return (int) face.getSections().stream()
            .filter(s -> s.getStatus() != null && s.getStatus().startsWith("OPEN"))
            .count();
    }
    
    default Integer countClosedSections(Face face) {
        if (face.getSections() == null) return 0;
        return (int) face.getSections().stream()
            .filter(s -> "CLOSED".equals(s.getStatus()))
            .count();
    }
}