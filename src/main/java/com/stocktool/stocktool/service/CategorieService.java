package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.CategorieDTO;
import com.stocktool.stocktool.entity.Categorie;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;

public interface CategorieService {

    Categorie saveCategorie(CategorieDTO categorieDTO);
    Categorie updateCategorie(Long id, CategorieDTO categorieDTO);
    Categorie findCategorieById(Long id);
    ResponseEntity<ResponseMessage> listofCategorie();
    void deleteCategorieById(Long id);
}
