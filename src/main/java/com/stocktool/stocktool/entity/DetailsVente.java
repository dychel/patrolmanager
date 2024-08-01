package com.stocktool.stocktool.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class DetailsVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date date;
    private double qte_av_vente;
    private double qte_restante;
    private double qte_vendue;
    private Long total_menu_vendu;
    private Float cout_unitaire;
    private Float cout_total;
    private Float benefice;
    @ManyToOne
    @JoinColumn(name = "menu_id", referencedColumnName = "id")
    private Menus menus;
    @ManyToOne
    @JoinColumn(name = "produit_id", referencedColumnName = "id")
    private Produit produit;
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "vente_id", referencedColumnName = "id")
    private Vente vente;

    public DetailsVente(Long id, Date date, double qte_av_vente, double qte_restante, double qte_vendue, Long total_menu_vendu, Float cout_unitaire, Float cout_total, Float benefice, Menus menus, Produit produit, Vente vente) {
        this.id = id;
        this.date = date;
        this.qte_av_vente = qte_av_vente;
        this.qte_restante = qte_restante;
        this.qte_vendue = qte_vendue;
        this.total_menu_vendu = total_menu_vendu;
        this.cout_unitaire = cout_unitaire;
        this.cout_total = cout_total;
        this.benefice = benefice;
        this.menus = menus;
        this.produit = produit;
        this.vente = vente;
    }

    public DetailsVente(Date date, double qte_av_vente, double qte_restante, double qte_vendue, Long total_menu_vendu, Float cout_unitaire, Float cout_total, Float benefice, Menus menus, Produit produit, Vente vente) {
        this.date = date;
        this.qte_av_vente = qte_av_vente;
        this.qte_restante = qte_restante;
        this.qte_vendue = qte_vendue;
        this.total_menu_vendu = total_menu_vendu;
        this.cout_unitaire = cout_unitaire;
        this.cout_total = cout_total;
        this.benefice = benefice;
        this.menus = menus;
        this.produit = produit;
        this.vente = vente;
    }
}
