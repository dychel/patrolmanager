package com.stocktool.stocktool.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Composition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "menu_id", referencedColumnName = "id")
    private Menus menus;
    @ManyToOne
    @JoinColumn(name = "produit_id", referencedColumnName = "id")
    private Produit produit;
    private String description;
    private Double quantite;
    private Date date;

    public Composition(Menus menus, Produit produit, String description, Double quantite, Date date) {
        this.menus = menus;
        this.produit = produit;
        this.description = description;
        this.quantite = quantite;
        this.date = date;
    }

    public Composition(Long id, Menus menus, Produit produit, String description, Double quantite, Date date) {
        this.id = id;
        this.menus = menus;
        this.produit = produit;
        this.description = description;
        this.quantite = quantite;
        this.date = date;
    }
}
