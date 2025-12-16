package com.analyfy.analify.Mapper;

import java.util.List;

public interface BaseMapper<T,E> {
    T toDto(E entity);
    E toEntity(T dto);
    
    List<T> toDtoList(List<E> entityList);
    List<E> toEntityList(List<T> dtoList);
}
