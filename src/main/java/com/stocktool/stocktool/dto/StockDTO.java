package com.stocktool.stocktool.dto;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
public class StockDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double qte_en_stock;
    private Date datemaj;
    private Long produitId;
}
