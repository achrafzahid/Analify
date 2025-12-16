package com.analyfy.analify.DTO;

import java.util.Date;

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
public class OrderDTO extends EntityBaseDTO<Long>{
    private Date orderDate;
    private Date shipDate;

    @ManyToOne(fetch=FetchType.LAZY)
    private CaissierDTO caissier;

    @ManyToOne(fetch=FetchType.LAZY)
    private OrderItemsDTO orderitem;

    
}
