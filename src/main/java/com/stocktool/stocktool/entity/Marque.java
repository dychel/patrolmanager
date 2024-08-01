package com.stocktool.stocktool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@Entity
public class Marque implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String marque;
    private String note;

    @OneToMany(mappedBy = "marque")
    @JsonIgnore
    private Set<Produit> produits;

    public Marque() {
    }

    public Marque(String marque, String note) {
        this.marque = marque;
        this.note = note;
    }
 
    public Marque(Long id, String marque, String note) {
        this.id = id;
        this.marque = marque;
        this.note = note;
    }
}
