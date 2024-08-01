package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.RavitaillementDTO;
import com.stocktool.stocktool.dto.StockDTO;
import com.stocktool.stocktool.entity.Ravitaillement;
import com.stocktool.stocktool.entity.Stock;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;

public interface StockService {
    Stock findStockById(Long id);
    Stock updateStock(Long id, StockDTO stockDTO);
    Stock saveStock(StockDTO StockDTO);
    ResponseEntity<ResponseMessage> listStock();
    void deleteStockById(Long id);
    Stock findStockByProduit(Long id);
}
