package com.stocktool.stocktool.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@NoArgsConstructor
public class Fournisseur {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private String type;
    private Date date_ajout;

    public Fournisseur(String nom, String email, String telephone, String adresse, String type, Date date_ajout) {
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.type = type;
        this.date_ajout = date_ajout;
    }

}
