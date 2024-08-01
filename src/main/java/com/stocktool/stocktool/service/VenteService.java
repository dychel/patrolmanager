package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.DetailsVenteDTO;
import com.stocktool.stocktool.dto.RavitaillementDTO;
import com.stocktool.stocktool.dto.StockDTO;
import com.stocktool.stocktool.dto.VenteDTO;
import com.stocktool.stocktool.entity.Ravitaillement;
import com.stocktool.stocktool.entity.Vente;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface VenteService {
    ResponseEntity<ResponseMessage> listVentes();
    Vente saveVente(VenteDTO venteDTO, StockDTO stockDTO, DetailsVenteDTO detailsVenteDTO);
    Vente updateVente(Long id, VenteDTO venteDTO);
    Vente findVenteById(Long id);
    List<Vente> getVenteByMenu(Long id);
    List<Vente> getVenteByUser(Long id);
    List<Vente> getVenteByEquipe(Long id);
    void deleteVenteById(Long id);
}
