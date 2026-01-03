package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.analyfy.analify.DTO.BidDTO;
import com.analyfy.analify.Entity.Bid;

@Mapper(componentModel = "spring")
public interface BidMapper extends BaseMapper<BidDTO, Bid> {
    
    @Override
    @Mapping(source = "section.sectionId", target = "sectionId")
    @Mapping(source = "section.sectionName", target = "sectionName")
    @Mapping(source = "section.basePrice", target = "sectionBasePrice")
    @Mapping(source = "section.currentPrice", target = "sectionCurrentPrice")
    @Mapping(source = "section.status", target = "sectionStatus")
    @Mapping(source = "investor.userId", target = "investorId")
    @Mapping(source = "investor.userName", target = "investorName")
    @Mapping(source = "investor.mail", target = "investorEmail")
    @Mapping(source = "section.face.faceName", target = "faceName")
    @Mapping(source = "section.face.rang.rangName", target = "rangName")
    @Mapping(source = "section.face.rang.category.categoryName", target = "categoryName")
    BidDTO toDto(Bid entity);
}