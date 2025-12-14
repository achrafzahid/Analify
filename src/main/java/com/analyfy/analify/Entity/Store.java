package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Store extends EntityBase<Long> {
    @OneToOne
    private adminStore adminStore;
    @OneToMany(mappedBy="store")
    private List<Caissier> caissier;
    @OneToOne
    private City city;

    @ManyToOne(fetch=FetchType.LAZY)
    private ProductItems productitem;

}
