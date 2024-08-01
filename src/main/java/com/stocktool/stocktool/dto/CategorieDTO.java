package com.stocktool.stocktool.dto;

import com.stocktool.stocktool.entity.Categorie;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorieDTO {

    private Long id;
    @Size(min = 2, message = "Your category must have 2 characters")
    private String intitule;
    private String description;
    private String category_code;

    public CategorieDTO(Categorie categorie) {
        this.intitule = categorie.getIntitule();
        this.description = categorie.getDescription();
        this.category_code = categorie.getCategory_code();
    }

    public Categorie toCategorie(){
        return new Categorie(intitule, description, category_code);
    }
}
