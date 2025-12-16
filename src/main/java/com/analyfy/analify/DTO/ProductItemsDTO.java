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
public class ProductItemsDTO extends EntityBaseDTO<Long>{
    private Integer quanity;

    @OneToMany(mappedBy="productitem")
    private List<ProductDTO> product;

    @OneToMany(mappedBy="productitem")
    private List<StoreDTO> store;

    
    
}
