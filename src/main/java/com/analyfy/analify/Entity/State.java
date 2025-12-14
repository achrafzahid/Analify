package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.ManyToOne;
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
public class State extends EntityBase<Long> {
    @OneToMany(mappedBy="State")
    private List<City> city;
    @ManyToOne
    private Region region;
    private String name;
}
