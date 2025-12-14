package com.analyfy.analify.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product extends EntityBase<Long>{
    private String name;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private SubCategory subCategory;

    @ManyToOne(fetch=FetchType.LAZY)
    private ProductItems productitem;

    @ManyToOne(fetch=FetchType.LAZY)
    private OrderItems orderitem;
}
