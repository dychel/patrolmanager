package com.stocktool.stocktool.dto;

import com.stocktool.stocktool.entity.Equipe;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipeDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private Date date_ajout;

    public EquipeDTO(Equipe equipe) {
        this.nom = equipe.getNom();
        this.date_ajout = equipe.getDate_ajout();
    }

    public Equipe toEquipe(){
        return new Equipe(nom, date_ajout);
    }
}
