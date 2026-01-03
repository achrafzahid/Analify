package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.analyfy.analify.DTO.SectionDTO;
import com.analyfy.analify.Entity.Section;

@Mapper(componentModel = "spring")
public interface SectionMapper extends BaseMapper<SectionDTO, Section> {
    
    @Override
    @Mapping(source = "face.faceId", target = "faceId")
    @Mapping(source = "face.faceName", target = "faceName")
    @Mapping(source = "face.rang.rangId", target = "rangId")
    @Mapping(source = "face.rang.rangName", target = "rangName")
    @Mapping(source = "face.rang.category.categoryId", target = "categoryId")
    @Mapping(source = "face.rang.category.categoryName", target = "categoryName")
    @Mapping(source = "winnerInvestor.userId", target = "winnerInvestorId")
    @Mapping(source = "winnerInvestor.userName", target = "winnerInvestorName")
    @Mapping(target = "totalBids", expression = "java(entity.getBids() != null ? entity.getBids().size() : 0)")
    @Mapping(target = "uniqueBidders", expression = "java(countUniqueBidders(entity))")
    @Mapping(target = "winningAmount", expression = "java(getWinningAmount(entity))")
    SectionDTO toDto(Section entity);
    
    default Integer countUniqueBidders(Section section) {
        if (section.getBids() == null) return 0;
        return (int) section.getBids().stream()
            .map(bid -> bid.getInvestor().getUserId())
            .distinct()
            .count();
    }
    
    default Double getWinningAmount(Section section) {
        if (section.getBids() == null || section.getBids().isEmpty()) return null;
        return section.getBids().stream()
            .filter(bid -> "WINNER".equals(bid.getStatus()))
            .map(bid -> bid.getAmount())
            .findFirst()
            .orElse(null);
    }
}