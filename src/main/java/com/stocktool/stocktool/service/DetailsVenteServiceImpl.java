package com.stocktool.stocktool.service;
import com.stocktool.stocktool.dto.DetailsVenteDTO;
import com.stocktool.stocktool.dto.ProduitDTO;
import com.stocktool.stocktool.entity.DetailsVente;
import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.DetailsStockRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DetailsVenteServiceImpl implements DetailsVenteService{
    @Autowired
    DetailsStockRepository detailsStockRepository;
    @Autowired
    VenteService venteService;
    @Autowired
    MenusService menusService;
    @Autowired
    ProduitService produitService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ResponseEntity<ResponseMessage> listDetailsVentes() {
        if (detailsStockRepository.count()==0)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "Pas de detail vente disponible", null), HttpStatus.OK);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Liste des details de ventes", detailsStockRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public DetailsVente saveDetailVente(DetailsVenteDTO detailsVenteDTO) {
        DetailsVente detailsVente = modelMapper.map(detailsVenteDTO, DetailsVente.class);
        return getDetailsVenteSaved(detailsVenteDTO, detailsVente);
    }

    private DetailsVente getDetailsVenteSaved(DetailsVenteDTO detailsVenteDTO, DetailsVente detailsVente) {
        updateForeignKeyDetailsVente(detailsVenteDTO, detailsVente);
        return detailsStockRepository.save(detailsVente);
    }

    private void updateForeignKeyDetailsVente(DetailsVenteDTO detailsVenteDTO, DetailsVente detailsVente) {
        if (detailsVenteDTO.getProduitId() != null)
            detailsVente.setProduit(produitService.findProduitById(detailsVenteDTO.getProduitId()));

        if (detailsVenteDTO.getVenteId() != null)
            detailsVente.setVente(venteService.findVenteById(detailsVenteDTO.getVenteId()));

        if (detailsVenteDTO.getMenuId() != null)
            detailsVente.setMenus(menusService.findMenusById(detailsVenteDTO.getMenuId()));
    }

    @Override
    public DetailsVente updateDetailVente(Long id, DetailsVenteDTO detailsVenteDTO) {
        DetailsVente detailsVenteToUpdate = detailsStockRepository.findByIdDetailsVente(id);

        if (detailsVenteToUpdate == null)
            throw new ApiRequestException("Detail vente non disponible");

        DetailsVente detailsVente = modelMapper.map(detailsVenteDTO, DetailsVente.class);

        detailsVente.setId(detailsVenteToUpdate.getId());

        // MAJ produit, menu, vente
        updateForeignKeyDetailsVente(detailsVenteDTO, detailsVente);

        return detailsStockRepository.save(detailsVente);
    }

    @Override
    public DetailsVente findDetailVenteById(Long id) {
        DetailsVente detailsVente = detailsStockRepository.findByIdDetailsVente(id);
        if (detailsVente==null)
            throw new ApiRequestException("Pas de detail de cette vente non disponible");
        return detailsStockRepository.findByIdDetailsVente(id);
    }

    @Override
    public List<DetailsVente> getDetailVenteByProduit(Long id) {
        return detailsStockRepository.findDetailsVenteByProduit(id);
    }

    @Override
    public List<DetailsVente> getDetailsVenteByMenu(Long id) {
        return detailsStockRepository.findDetailsVenteByMenu(id);
    }

    @Override
    public List<DetailsVente> getDetailsVenteByVente(Long id) {
        return detailsStockRepository.findDetailsVenteByVente(id);
    }

    @Override
    public void deleteDetailsVenteById(Long id) {
        DetailsVente detailsVente = detailsStockRepository.findByIdDetailsVente(id);
        if (detailsVente==null){
            throw new ApiRequestException("Detail vente non disponible");
        }else{
            detailsStockRepository.deleteById(id);
        }
    }
}
