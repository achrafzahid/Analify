package com.analyfy.analify.Entity;

import java.time.LocalDate;
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
@Table(name = "section")
@Getter @Setter
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "section_name", nullable = false)
    private String sectionName;

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @Column(name = "current_price", nullable = false)
    private Double currentPrice;

    @Column(name = "status", nullable = false)
    private String status = "OPEN"; 
    @Column(name = "date_delai")
    private LocalDate dateDelai; // Date limite de l'enchère

    @Column(name = "description")
    private String description;

    // Relation avec Face
    @ManyToOne
    @JoinColumn(name = "face_id", nullable = false)
    private Face face;

    // Relation avec Bid (enchères)
    @OneToMany(mappedBy = "section")
    private List<Bid> bids;

    // Investisseur gagnant (si vendu)
    @ManyToOne
    @JoinColumn(name = "winner_investor_id")
    private Investor winnerInvestor;

    /**@ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;*/
}