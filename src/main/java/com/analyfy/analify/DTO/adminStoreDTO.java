package com.analyfy.analify.DTO;

import java.util.Date;

import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class adminStoreDTO extends UserDTO {
    @OneToOne
    private StoreDTO store;
    private Date date_started;
    private Double salary;



    
}
