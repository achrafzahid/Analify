package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class SubCategory extends EntityBase<Long>{
    private String sub_name;
    @OneToMany(mappedBy="subCategory")
    private List<Product> product;
    @ManyToOne(fetch=FetchType.LAZY)
    private Category category;
}
