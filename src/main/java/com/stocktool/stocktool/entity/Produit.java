package com.stocktool.stocktool.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.io.File;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
public class Produit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String libelle;
    @Column(columnDefinition = "TEXT")
    private String description;
    private Date date;
    @ManyToOne
    @JoinColumn(name = "unite_id", referencedColumnName = "id")
    private Unite unite;
    @ManyToOne
    @JoinColumn(name = "marque_id", referencedColumnName = "id")
    private Marque marque;
    @ManyToOne
    @JoinColumn(name = "categorie_id", referencedColumnName = "id")
    private Categorie categorie;
    private Long bar_code;
    private String type;
    private Boolean disponibilite;
    private Long seuilminimun;
    private File image;
    private float prix_achat;

    public Produit() {
    }

    public Produit(String libelle, String description, Date date, Unite unite, Marque marque, Categorie categorie, Long bar_code, String type, Boolean disponibilite, Long seuilminimun, File image) {
        this.libelle = libelle;
        this.description = description;
        this.date = date;
        this.unite = unite;
        this.marque = marque;
        this.categorie = categorie;
        this.bar_code = bar_code;
        this.type = type;
        this.disponibilite = disponibilite;
        this.seuilminimun = seuilminimun;
        this.image = image;
    }

    public Produit(Long id, String libelle, String description, Date date, Unite unite, Marque marque, Categorie categorie, Long bar_code, String type, Boolean disponibilite, Long seuilminimun, File image) {
        this.id = id;
        this.libelle = libelle;
        this.description = description;
        this.date = date;
        this.unite = unite;
        this.marque = marque;
        this.categorie = categorie;
        this.bar_code = bar_code;
        this.type = type;
        this.disponibilite = disponibilite;
        this.seuilminimun = seuilminimun;
        this.image = image;
    }

}
