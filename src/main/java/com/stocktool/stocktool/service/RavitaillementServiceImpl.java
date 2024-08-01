package com.stocktool.stocktool.service;
import com.stocktool.stocktool.dto.RavitaillementDTO;
import com.stocktool.stocktool.dto.StockDTO;
import com.stocktool.stocktool.entity.Ravitaillement;
import com.stocktool.stocktool.entity.Stock;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.RavitaillementRepository;
import com.stocktool.stocktool.repository.StockRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RavitaillementServiceImpl implements RavitaillementService{

    @Autowired
    RavitaillementRepository ravitaillementRepository;
    @Autowired
    StockRepository stockRepository;
    @Autowired
    StockService stockService;
    @Autowired
    ProduitService produitService;
    @Autowired
    FournisseurService fournisseurService;
    @Autowired
    private ModelMapper modelMapper;

    Long currentTimeInMillis = System.currentTimeMillis();
    Date currentDate;

    @Override
    public Ravitaillement saveRavitaillement(RavitaillementDTO ravitaillementDTO, StockDTO stockDTO) {
        Ravitaillement ravitaillement = modelMapper.map(ravitaillementDTO, Ravitaillement.class);
        return getRavitaillementSaved(ravitaillementDTO, ravitaillement, stockDTO);
    }

    @Override
    public Ravitaillement updateRavitaillement(Long id, RavitaillementDTO ravitaillementDTO) {
        Ravitaillement ravitaillementToUpdate = ravitaillementRepository.findByIdRavitaillement(id);

        if (ravitaillementToUpdate == null)
            throw new ApiRequestException("Ravitaillement non effectué");
        Ravitaillement ravitaillement = modelMapper.map(ravitaillementDTO, Ravitaillement.class);
        ravitaillement.setId(ravitaillementToUpdate.getId());
        // MAJ des tables unit, brand et Catégories liées à Product
        updateForeignKeyRavitaillement(ravitaillementDTO, ravitaillement);
        return ravitaillementRepository.save(ravitaillement);
    }

    private Ravitaillement getRavitaillementSaved(RavitaillementDTO ravitaillementDTO, Ravitaillement ravitaillement, StockDTO stockDTO) {
        updateForeignKeyRavitaillement(ravitaillementDTO, ravitaillement);

        Stock stock_update = stockService.findStockByProduit(ravitaillementDTO.getProduitId());

        if (stock_update!=null){
            ravitaillement.setQte_av_livraison(stock_update.getQte_en_stock());
        }else{
            ravitaillement.setQte_av_livraison(0.0);
        }
        ravitaillement.setQte_total(ravitaillement.getQte_livre() + ravitaillement.getQte_av_livraison());
        updateStock(ravitaillementDTO, stockDTO);
        return ravitaillementRepository.save(ravitaillement);
    }

    private void updateForeignKeyRavitaillement(RavitaillementDTO ravitaillementDTO, Ravitaillement ravitaillement) {
        if (ravitaillementDTO.getProduitId() != null)
            ravitaillement.setProduit(produitService.findProduitById(ravitaillementDTO.getProduitId()));
        if (ravitaillementDTO.getFournisseurId() != null)
            ravitaillement.setFournisseur(fournisseurService.findFournisseurById(ravitaillementDTO.getFournisseurId()));
    }

    private void updateStock(RavitaillementDTO ravitaillementDTO, StockDTO stockDTO){

        currentDate = new Date(currentTimeInMillis);

        Stock stock_update = stockService.findStockByProduit(ravitaillementDTO.getProduitId());
        // si un id stock exist dans stock_update, on met à jour sinon on rajoute
        if(stock_update==null){
            stockDTO.setQte_en_stock(ravitaillementDTO.getQte_livre());
            stockDTO.setProduitId(ravitaillementDTO.getProduitId());
            stockDTO.setDatemaj(currentDate);
            stockService.saveStock(stockDTO);
        }else{
            stockDTO.setQte_en_stock(stock_update.getQte_en_stock() + ravitaillementDTO.getQte_livre());
            stockDTO.setProduitId(ravitaillementDTO.getProduitId());
            stockDTO.setDatemaj(currentDate);
            stockService.updateStock(stock_update.getId(), stockDTO);
        }

    }

    @Override
    public Ravitaillement findRavitaillementById(Long id) {
        Ravitaillement ravitaillement = ravitaillementRepository.findByIdRavitaillement(id);
        if (ravitaillement==null)
            throw new ApiRequestException("Produit non disponible");
        return ravitaillementRepository.findByIdRavitaillement(id);
    }

    @Override
    public ResponseEntity<ResponseMessage> listRavitaillements() {
        if (ravitaillementRepository.count()==0)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "Pas de produit ravitaillés", null), HttpStatus.OK);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Liste des produit ravitaillés", ravitaillementRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public void deleteRavitaillementById(Long id) {
        Ravitaillement ravitaillement = ravitaillementRepository.findByIdRavitaillement(id);
        if (ravitaillement==null){
            throw new ApiRequestException("Pas de ravitaillement correspondant!");
        }else{
            ravitaillementRepository.deleteById(id);
        }
    }

    @Override
    public List<Ravitaillement> getRavitaillementByProduit(Long id) {
        return ravitaillementRepository.findRavitaillementByProduit(id);
    }

    @Override
    public Ravitaillement getRavitByLastProd(Long id) {
        return ravitaillementRepository.findRavitaillementById(id);
    }

    @Override
    public List<Ravitaillement> getRavitaillementByFournisseur(Long id) {
        return ravitaillementRepository.findProduitByFournisseur(id);
    }
}
