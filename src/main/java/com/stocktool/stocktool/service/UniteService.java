package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.UniteDTO;
import com.stocktool.stocktool.entity.Unite;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;

public interface UniteService {
    Unite saveUnite(UniteDTO uniteDTO);
    Unite updateUnite(Long id, UniteDTO uniteDTO);
    Unite findUniteById(Long id);
    ResponseEntity<ResponseMessage> listofUnite();
    void deleteUniteById(Long id);
}
