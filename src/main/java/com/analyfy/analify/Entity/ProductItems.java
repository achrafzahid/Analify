package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductItems extends EntityBase<Long>{
    private Integer quanity;

    @OneToMany(mappedBy="productitem")
    private List<Product> product;

    @OneToMany(mappedBy="productitem")
    private List<Store> store;

    
    
}
