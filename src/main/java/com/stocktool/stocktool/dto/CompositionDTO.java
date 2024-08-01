package com.stocktool.stocktool.dto;
import com.stocktool.stocktool.entity.Menus;
import com.stocktool.stocktool.entity.Produit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
public class CompositionDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long menuId;
    private Long produitId;
    private String description;
    private Double quantite;
    private Date date;
}
