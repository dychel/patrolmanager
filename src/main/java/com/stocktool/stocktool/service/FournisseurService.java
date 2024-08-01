package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.CategorieDTO;
import com.stocktool.stocktool.dto.FournisseurDTO;
import com.stocktool.stocktool.entity.Categorie;
import com.stocktool.stocktool.entity.Fournisseur;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

public interface FournisseurService {
    Fournisseur saveFournisseur(FournisseurDTO fournisseurDTO);
    Fournisseur updateFournisseur(Long id, FournisseurDTO fournisseurDTO);
    Fournisseur findFournisseurById(Long id);
    ResponseEntity<ResponseMessage> listofFournisseur();
    void deleteFournisseurById(Long id);
}
