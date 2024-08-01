package com.stocktool.stocktool.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double qte_en_stock;
    private Date datemaj;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "produit_id", referencedColumnName = "id")
    private Produit produit;

    public Stock() {
    }

    public Stock(Double qte_en_stock, Date datemaj, Produit produit) {
        this.qte_en_stock = qte_en_stock;
        this.datemaj = datemaj;
        this.produit = produit;
    }

    public Stock(Long id, Double qte_en_stock, Date datemaj, Produit produit) {
        this.id = id;
        this.qte_en_stock = qte_en_stock;
        this.datemaj = datemaj;
        this.produit = produit;
    }
}
