package com.stocktool.stocktool.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.entity.Unite;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor

public class UniteDTO {

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

    public UniteDTO(Unite unite) {
        this.intitule = unite.getIntitule();
        this.unitedebase = unite.getUnitedebase();
        this.nomcourt = unite.getNomcourt();
        this.allowDecimal = unite.getAllowDecimal();
    }
    public Unite toUnite(){
        return new Unite(intitule, unitedebase, nomcourt, allowDecimal);
    }
}
