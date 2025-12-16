package com.analyfy.analify.DTO;


import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO extends EntityBaseDTO<Long>{
    private String name;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private SubCategoryDTO subCategory;

    @ManyToOne(fetch=FetchType.LAZY)
    private ProductItemsDTO productitem;

    @ManyToOne(fetch=FetchType.LAZY)
    private OrderItemsDTO orderitem;
}
