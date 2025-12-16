package com.analyfy.analify.DTO;

import java.util.List;

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
public class StateDTO extends EntityBaseDTO<Long> {
    @OneToMany(mappedBy="state")
    private List<CityDTO> city;
    @ManyToOne
    private RegionDTO region;
    private String name;
}
