package com.analyfy.analify.Mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


public interface BaseMapper<D, E> {
    D toDto(E entity);
    E toEntity(D dto);
    List<D> toDtoList(List<E> entityList);
    List<E> toEntityList(List<D> dtoList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}