package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Investor")
@Getter @Setter
@PrimaryKeyJoinColumn(name = "user_id")
public class Investor extends User {

    // Inherits ID and fields from User

    @OneToMany(mappedBy="id_inv")
    List<Product> products;

    // pour la section 
    @OneToMany(mappedBy = "winnerInvestor")
    private List<Section> wonSections;

}