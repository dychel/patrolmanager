package com.stocktool.stocktool.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Categorie implements Serializable {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String intitule;
    private String description;
    private String category_code;

    @OneToMany(mappedBy = "categorie")
    @JsonIgnore
    private List<Produit> produits;

    public Categorie(String intitule, String description, String category_code) {
        this.intitule = intitule;
        this.description = description;
        this.category_code = category_code;
    }

    public Categorie(Long id, String intitule, String description, String category_code) {
        this.id = id;
        this.intitule = intitule;
        this.description = description;
        this.category_code = category_code;
    }
}
