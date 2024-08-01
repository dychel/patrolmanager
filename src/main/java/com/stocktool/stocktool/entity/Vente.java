package com.stocktool.stocktool.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Vente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private String type;
    @ManyToOne
    @JoinColumn(name = "menu_id", referencedColumnName = "id")
    private Menus menus;
    private Long total_vendu;
    private Float prix_de_vente;
    private Float prix_total;
    private String autres;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "equipe_id", referencedColumnName = "id")
    private Equipe equipe;
    private Date date;

    public Vente(String description, String type, Menus menus, Long total_vendu, Float prix_de_vente, Float prix_total, String autres, User user, Equipe equipe, Date date) {
        this.description = description;
        this.type = type;
        this.menus = menus;
        this.total_vendu = total_vendu;
        this.prix_de_vente = prix_de_vente;
        this.prix_total = prix_total;
        this.autres = autres;
        this.user = user;
        this.equipe = equipe;
        this.date = date;
    }

    public Vente(Long id, String description, String type, Menus menus, Long total_vendu, Float prix_de_vente, Float prix_total, String autres, User user, Equipe equipe, Date date) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.menus = menus;
        this.total_vendu = total_vendu;
        this.prix_de_vente = prix_de_vente;
        this.prix_total = prix_total;
        this.autres = autres;
        this.user = user;
        this.equipe = equipe;
        this.date = date;
    }
}
