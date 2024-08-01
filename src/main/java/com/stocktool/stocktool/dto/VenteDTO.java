package com.stocktool.stocktool.dto;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
public class VenteDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private String type;
    private Long menuId;
    private Long total_vendu;
    private Float prix_de_vente;
    private Float pix_total;
    private String autres;
    private Long equipeId;
    private Long userId;
   // private Date date;

}
