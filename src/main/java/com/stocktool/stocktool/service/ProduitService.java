package com.stocktool.stocktool.service;
import com.stocktool.stocktool.dto.ProduitDTO;
import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProduitService {
    Produit saveProduit(ProduitDTO produitDTO);
    Produit updateProduit(Long id, ProduitDTO produitDTO);
    Produit findProduitById(Long id);
    ResponseEntity<ResponseMessage> listProduits();
    void deleteProduitById(Long id);
    List<Produit> getProduitByUnit(Long id);
    List<Produit> getProduitByMarque(Long id);
    List<Produit> getProduitByCategories(Long id);
}
