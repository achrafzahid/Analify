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
@Table(name = "face")
@Getter @Setter
public class Face {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "face_id")
    private Long faceId;

    @Column(name = "face_name", nullable = false)
    private String faceName;

    // description 
    @Column(name = "description")
    private String description;

    // Relation avec Rang
    @ManyToOne
    @JoinColumn(name = "rang_id", nullable = false)
    private Rang rang;


    // Relation avec Section
    @OneToMany(mappedBy = "face")
    private List<Section> sections;
}