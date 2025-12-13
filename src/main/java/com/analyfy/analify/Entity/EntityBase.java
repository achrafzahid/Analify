package com.analyfy.analify.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;


@MappedSuperclass
@Data
public abstract class EntityBase<ID> {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private ID id;
    
}
