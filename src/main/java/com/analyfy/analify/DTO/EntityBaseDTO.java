package com.analyfy.analify.DTO;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;


@MappedSuperclass
@Data
public abstract class EntityBaseDTO<ID> {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private ID id;
    
}
