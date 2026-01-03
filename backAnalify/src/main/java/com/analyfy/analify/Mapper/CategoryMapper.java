package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.analyfy.analify.DTO.CategoryDTO;
import com.analyfy.analify.Entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper extends BaseMapper<CategoryDTO, Category> {
    
    @Override
    @Mapping(target = "totalRangs", expression = "java(entity.getRangs() != null ? entity.getRangs().size() : 0)")
    @Mapping(target = "totalFaces", expression = "java(countTotalFaces(entity))")
    @Mapping(target = "totalSections", expression = "java(countTotalSections(entity))")
    @Mapping(target = "activeBids", expression = "java(countActiveBids(entity))")
    CategoryDTO toDto(Category entity);
    
    default Integer countTotalFaces(Category category) {
        if (category.getRangs() == null) return 0;
        return category.getRangs().stream()
            .mapToInt(rang -> rang.getFaces() != null ? rang.getFaces().size() : 0)
            .sum();
    }
    
    default Integer countTotalSections(Category category) {
        if (category.getRangs() == null) return 0;
        return category.getRangs().stream()
            .flatMap(rang -> rang.getFaces().stream())
            .mapToInt(face -> face.getSections() != null ? face.getSections().size() : 0)
            .sum();
    }
    
    default Integer countActiveBids(Category category) {
        if (category.getRangs() == null) return 0;
        return (int) category.getRangs().stream()
            .flatMap(rang -> rang.getFaces().stream())
            .flatMap(face -> face.getSections().stream())
            .flatMap(section -> section.getBids() != null ? section.getBids().stream() : java.util.stream.Stream.empty())
            .filter(bid -> "WINNER".equals(bid.getStatus()))
            .count();
    }
}