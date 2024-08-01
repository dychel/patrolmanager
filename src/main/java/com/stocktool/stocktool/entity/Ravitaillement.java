package com.stocktool.stocktool.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.engine.internal.Cascade;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Ravitaillement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String libelle;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToOne
    @JoinColumn(name = "produit_id", referencedColumnName = "id")
    private Produit produit;
    @ManyToOne
    @JoinColumn(name = "fournisseur_id", referencedColumnName = "id")
    private Fournisseur fournisseur;
    private Double qte_livre;
    private Float prixuntaire;
    private Float prixtotal;
    private Double qte_total;
    private Double qte_av_livraison;
    private Date date;
    private Date datemaj;

    public Ravitaillement(String libelle, String description, Produit produit, Fournisseur fournisseur, Double qte_livre, Float prixuntaire, Float prixtotal, Double qte_total, Double qte_av_livraison, Date date, Date datemaj) {
        this.libelle = libelle;
        this.description = description;
        this.produit = produit;
        this.fournisseur = fournisseur;
        this.qte_livre = qte_livre;
        this.prixuntaire = prixuntaire;
        this.prixtotal = prixtotal;
        this.qte_total = qte_total;
        this.qte_av_livraison = qte_av_livraison;
        this.date = date;
        this.datemaj = datemaj;
    }

    public Ravitaillement(Long id, String libelle, String description, Produit produit, Fournisseur fournisseur, Double qte_livre, Float prixuntaire, Float prixtotal, Double qte_total, Double qte_av_livraison, Date date, Date datemaj) {
        this.id = id;
        this.libelle = libelle;
        this.description = description;
        this.produit = produit;
        this.fournisseur = fournisseur;
        this.qte_livre = qte_livre;
        this.prixuntaire = prixuntaire;
        this.prixtotal = prixtotal;
        this.qte_total = qte_total;
        this.qte_av_livraison = qte_av_livraison;
        this.date = date;
        this.datemaj = datemaj;
    }
}
