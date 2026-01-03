package com.analyfy.analify.Entity;

import java.util.ArrayList;
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
@Table(name = "subcategory")
@Getter @Setter
public class Subcategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subcategory_id")
    private Long subId;

    @Column(name = "subcategory_name")
    private String subName;

    // Link Upwards to Category
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // THE FIX: Link Downwards to Products
    // "mappedBy" refers to the 'subcategory' field in the Product class
    @OneToMany(mappedBy = "subcategory")
    private List<Product> products = new ArrayList<>();
}