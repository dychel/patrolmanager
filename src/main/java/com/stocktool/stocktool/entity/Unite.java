package com.stocktool.stocktool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
public class Unite implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String intitule;
    private String unitedebase;
    private String nomcourt;
    private Boolean allowDecimal;

    @OneToMany(mappedBy = "unite")
    @JsonIgnore
    private Set<Produit> poduits;

    public Unite(String intitule, String unitedebase, String nomcourt, Boolean allowDecimal) {
        this.intitule = intitule;
        this.unitedebase = unitedebase;
        this.nomcourt = nomcourt;
        this.allowDecimal = allowDecimal;
    }

    public Unite(Long id, String intitule, String unitedebase, String nomcourt, Boolean allowDecimal) {
        this.id = id;
        this.intitule = intitule;
        this.unitedebase = unitedebase;
        this.nomcourt = nomcourt;
        this.allowDecimal = allowDecimal;
    }
}
