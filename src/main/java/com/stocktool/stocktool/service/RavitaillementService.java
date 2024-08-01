package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.ProduitDTO;
import com.stocktool.stocktool.dto.RavitaillementDTO;
import com.stocktool.stocktool.dto.StockDTO;
import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.entity.Ravitaillement;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RavitaillementService {
    Ravitaillement saveRavitaillement(RavitaillementDTO ravitaillementDTO, StockDTO stockDTO);
    Ravitaillement updateRavitaillement(Long id, RavitaillementDTO ravitaillementDTO);
    Ravitaillement findRavitaillementById(Long id);
    ResponseEntity<ResponseMessage> listRavitaillements();
    void deleteRavitaillementById(Long id);
    List<Ravitaillement> getRavitaillementByProduit(Long id);

    Ravitaillement getRavitByLastProd(Long id);

    List<Ravitaillement> getRavitaillementByFournisseur(Long id);
}
