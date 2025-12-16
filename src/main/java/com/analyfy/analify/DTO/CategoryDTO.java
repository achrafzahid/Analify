package com.analyfy.analify.DTO;

import java.util.List;

import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO extends EntityBaseDTO<Long>{
    private String name;
    @OneToMany(mappedBy="category")
    private List<SubCategoryDTO> subcategory;
    

}
