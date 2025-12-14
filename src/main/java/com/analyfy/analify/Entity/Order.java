package com.analyfy.analify.Entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order extends EntityBase<Long>{
    private Date orderDate;
    private Date shipDate;

    @ManyToOne(fetch=FetchType.LAZY)
    private Caissier caissier;

    @ManyToOne(fetch=FetchType.LAZY)
    private OrderItems orderitem;

    
}
