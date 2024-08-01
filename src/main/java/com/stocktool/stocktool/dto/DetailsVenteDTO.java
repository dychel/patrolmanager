package com.stocktool.stocktool.dto;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
@Data
public class DetailsVenteDTO {
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
    private Long menuId;
    private Long produitId;
    private Long venteId;

}
