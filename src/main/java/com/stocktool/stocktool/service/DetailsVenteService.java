package com.stocktool.stocktool.service;


import com.stocktool.stocktool.dto.DetailsVenteDTO;
import com.stocktool.stocktool.dto.StockDTO;
import com.stocktool.stocktool.dto.VenteDTO;
import com.stocktool.stocktool.entity.DetailsVente;
import com.stocktool.stocktool.entity.Vente;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DetailsVenteService {
    ResponseEntity<ResponseMessage> listDetailsVentes();
    DetailsVente saveDetailVente(DetailsVenteDTO detailsVenteDTO);
    DetailsVente updateDetailVente(Long id, DetailsVenteDTO detailsVenteDTO);
    DetailsVente findDetailVenteById(Long id);
    List<DetailsVente> getDetailVenteByProduit(Long id);
    List<DetailsVente> getDetailsVenteByMenu(Long id);
    List<DetailsVente> getDetailsVenteByVente(Long id);
    void deleteDetailsVenteById(Long id);
}
