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
public class OrderItemsDTO extends EntityBaseDTO<Long>{
    private Double price;
    private Integer quantity;
    private Double discount = null;


    @OneToMany(mappedBy="orderitem")
    private List<ProductDTO> product;
    
    @OneToMany(mappedBy="orderitem")
    private List<OrderDTO> order;
}
