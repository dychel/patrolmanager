package com.stocktool.stocktool.dto;
import com.stocktool.stocktool.entity.Fournisseur;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FournisseurDTO {
    private Long id;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private String type;
    private Date date_ajout;

    public FournisseurDTO(Fournisseur fournisseur) {
        this.nom = fournisseur.getNom();
        this.email = fournisseur.getEmail();
        this.telephone = fournisseur.getTelephone();
        this.adresse = fournisseur.getAdresse();
        this.type = fournisseur.getType();
        this.date_ajout = fournisseur.getDate_ajout();
    }

    public Fournisseur toFournisseur(){
        return new Fournisseur(nom, email, telephone,adresse,type,date_ajout);
    }
}
