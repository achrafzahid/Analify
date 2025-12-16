package com.analyfy.analify.DTO;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CityDTO extends EntityBaseDTO<Long> {
    @OneToOne
    private StoreDTO store;
    @ManyToOne(fetch=FetchType.LAZY)
    private StateDTO state;
    private String name;
}
