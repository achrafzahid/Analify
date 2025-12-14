package com.analyfy.analify.Entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order extends EntityBase<Long>{
    private Date orderDate;
    private Date shipDate;

    @ManyToOne(fetch=FetchType.LAZY)
    private adminStore adminstore;

    @ManyToOne(fetch=FetchType.LAZY)
    private OrderItems orderitem;

    
}
