package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

public class SubCategory extends EntityBase<Long>{
    private String sub_name;
    @OneToMany(mappedBy="subcategory")
    private List<Product> product;
    @ManyToOne
    private Category category;
}
