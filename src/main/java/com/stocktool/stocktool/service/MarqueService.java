package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.MarqueDTO;
import com.stocktool.stocktool.entity.Marque;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;

public interface MarqueService {

    Marque saveMarque(MarqueDTO marqueDTO);
    Marque updateMarque(Long id, MarqueDTO marqueDTO);
    Marque findMarqueById(Long id);
    ResponseEntity<ResponseMessage> listofMarque();
    void deleteMarqueById(Long id);
}
