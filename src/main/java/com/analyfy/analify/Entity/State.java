package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class State extends EntityBase<Long> {
    @OneToMany(mappedBy="state")
    private List<City> city;
    @ManyToOne
    private Region region;
    private String name;
}
