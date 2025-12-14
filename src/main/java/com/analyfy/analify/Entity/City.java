package com.analyfy.analify.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class City extends EntityBase<Long> {
    @OneToOne
    private Store store;
    @ManyToOne(fetch=FetchType.LAZY)
    private State state;
    private String name;
}
