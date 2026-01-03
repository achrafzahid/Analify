package com.analyfy.analify.Entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rang")
@Getter @Setter
public class Rang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rang_id")
    private Long rangId;

    @Column(name = "rang_name", nullable = false)
    private String rangName;

    // description 
    @Column(name = "description")
    private String description;

    // Relation avec Category
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Relation avec Face
    @OneToMany(mappedBy = "rang")
    private List<Face> faces;
}