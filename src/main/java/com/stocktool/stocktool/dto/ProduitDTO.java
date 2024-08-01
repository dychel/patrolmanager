package com.stocktool.stocktool.dto;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
public class ProduitDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty
    @Size(min = 2, message = "Your product name must have 2 characters")
    private String libelle;
    private String description;
    private Date date_ajout;
    private Long uniteId;
    private Long marqueId;
    private Long categorieId;
    private Long bar_code;
    private String type;
    private Boolean disponibilite;
    private Long seuilminimun;
    private List<MultipartFile> images;
    private double prix_achat;

}
