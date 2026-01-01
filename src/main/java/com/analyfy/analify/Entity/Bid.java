package com.analyfy.analify.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bid")
@Getter @Setter
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long bidId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    // Relation avec Section
    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    // Relation avec Investor (celui qui enchérit)
    @ManyToOne
    @JoinColumn(name = "investor_id", nullable = false)
    private Investor investor;
}