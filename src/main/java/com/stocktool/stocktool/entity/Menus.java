package com.stocktool.stocktool.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.File;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
public class Menus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom_menu;
    private String type;
    private String description;
    private Date date_ajout;
    private String marge;
    private File images;
    private Float prix;

    public Menus(String nom_menu, String type, String description, Date date_ajout, String marge, File images, Float prix) {
        this.nom_menu = nom_menu;
        this.type = type;
        this.description = description;
        this.date_ajout = date_ajout;
        this.marge = marge;
        this.images = images;
        this.prix = prix;
    }

    public Menus(Long id, String nom_menu, String type, String description, Date date_ajout, String marge, File images, Float prix) {
        this.id = id;
        this.nom_menu = nom_menu;
        this.type = type;
        this.description = description;
        this.date_ajout = date_ajout;
        this.marge = marge;
        this.images = images;
        this.prix = prix;
    }
}
