package com.analyfy.analify.DTO;

import java.util.List;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SubCategoryDTO extends EntityBaseDTO<Long>{
    private String sub_name;
    @OneToMany(mappedBy="subCategory")
    private List<ProductDTO> product;
    @ManyToOne(fetch=FetchType.LAZY)
    private CategoryDTO category;
}
