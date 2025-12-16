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
public class RegionDTO extends EntityBaseDTO<Long>{
    @OneToMany(mappedBy="region")
    private List<StateDTO> states;
    private String name;
}
