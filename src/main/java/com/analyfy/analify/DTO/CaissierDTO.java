package com.analyfy.analify.DTO;

import java.util.Date;
import java.util.List;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CaissierDTO extends UserDTO {

    @ManyToOne(fetch=FetchType.LAZY)
    private StoreDTO store;

    @OneToMany(mappedBy="caissier")
    private List<OrderDTO> orders;
    
    private Date date_started;
    private Double salary;
}
