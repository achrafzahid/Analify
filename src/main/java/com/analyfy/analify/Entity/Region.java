package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.OneToMany;


import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Region extends EntityBase<Long>{
    @OneToMany(mappedBy="Region")
    private List<State> states;
    private String name;
}
