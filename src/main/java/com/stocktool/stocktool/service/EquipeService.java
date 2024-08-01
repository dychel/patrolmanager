package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.EquipeDTO;
import com.stocktool.stocktool.entity.Equipe;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;

public interface EquipeService {

    Equipe saveEquipe(EquipeDTO equipeDTO);
    Equipe updateEquipe(Long id, EquipeDTO equipeDTO);
    Equipe findEquipeById(Long id);
    ResponseEntity<ResponseMessage> listofequipe();
    void deleteEquipeById(Long id);
}
