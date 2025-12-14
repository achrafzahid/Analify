package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category extends EntityBase<Long>{
    private String name;
    @OneToMany(mappedBy="subcategory")
    private List<SubCategory> subcategory;
    

}
