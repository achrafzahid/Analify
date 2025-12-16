package com.analyfy.analify.DTO;

import java.util.List;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreDTO extends EntityBaseDTO<Long> {
    @OneToOne
    private adminStoreDTO adminStore;
    @OneToMany(mappedBy="store")
    private List<CaissierDTO> caissier;
    @OneToOne
    private CityDTO city;

    @ManyToOne(fetch=FetchType.LAZY)
    private ProductItemsDTO productitem;

}
