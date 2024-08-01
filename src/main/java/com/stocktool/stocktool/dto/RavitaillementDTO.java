package com.stocktool.stocktool.dto;
import com.stocktool.stocktool.entity.Fournisseur;
import jakarta.persistence.*;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class RavitaillementDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String libelle;
    private String description;
    @NotNull
    private Long produitId;
    @NotNull
    private Long fournisseurId;
    @NotNull
    private Double qte_livre;
    private Float prixunitaire;
    private Float prixtotal;
    private Double qte_total;
    @Basic(optional = true)
    @Column(nullable = true)
    private Date date_livraison;
    private Date datemaj;


}
