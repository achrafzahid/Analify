package com.analyfy.analify.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product extends EntityBase<Long>{
    private String name;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private SubCategory sub;

    @ManyToOne(fetch=FetchType.LAZY)
    private ProductItems productitem;

    @ManyToOne(fetch=FetchType.LAZY)
    private OrderItems orderitem;
}
