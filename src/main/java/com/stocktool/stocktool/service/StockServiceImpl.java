package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.RavitaillementDTO;
import com.stocktool.stocktool.dto.StockDTO;
import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.entity.Ravitaillement;
import com.stocktool.stocktool.entity.Stock;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.StockRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl implements StockService{

    @Autowired
    StockRepository stockRepository;
    @Autowired
    ProduitService produitService;

    @Autowired
    private ModelMapper modelMapper;
    @Override
    public Stock findStockById(Long id) {
        Stock stock = stockRepository.findByIdStock(id);
        if (stock==null)
            throw new ApiRequestException("Pas de produit en stock");
        return stockRepository.findByIdStock(id);
    }

    @Override
    public Stock updateStock(Long id, StockDTO stockDTO) {
        Stock stockToUpdate = stockRepository.findStockByProduit(id);

        if (stockToUpdate == null)
            throw new ApiRequestException("stock id non disponible");
        Stock stock = modelMapper.map(stockDTO, Stock.class);
        stock.setId(stockToUpdate.getId());
        updateForeignKeyStock(stockDTO, stock);

        return stockRepository.save(stock);
    }

    @Override
    public Stock saveStock(StockDTO stockDTO) {
        Stock stock = modelMapper.map(stockDTO, Stock.class);
        return getStockSaved(stockDTO, stock);
    }

    private Stock getStockSaved(StockDTO stockDTO, Stock stock) {
        updateForeignKeyStock(stockDTO, stock);
        return stockRepository.save(stock);
    }

    private void updateForeignKeyStock(StockDTO stockDTO, Stock stock) {
        if (stockDTO.getProduitId() != null)
            stock.setProduit(produitService.findProduitById(stockDTO.getProduitId()));
    }

    @Override
    public ResponseEntity<ResponseMessage> listStock() {
        if (stockRepository.count()==0)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "Pas de produit en stock", null), HttpStatus.OK);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Liste des produit en stock", stockRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public void deleteStockById(Long id) {
        Stock stock = stockRepository.findByIdStock(id);
        if (stock==null){
            throw new ApiRequestException("Pas de correspondance!");
        }else{
            stockRepository.deleteById(id);
        }
    }

    @Override
    public Stock findStockByProduit(Long id) {
        return stockRepository.findStockByProduit(id);

    }
}
