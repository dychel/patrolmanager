package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.CompositionDTO;
import com.stocktool.stocktool.dto.ProduitDTO;
import com.stocktool.stocktool.entity.Composition;
import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.CompositionRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompositionServiceImpl implements CompositionService{

    @Autowired
    CompositionRepository compositionRepository;
    @Autowired
    ProduitService produitService;
    @Autowired
    MenusService menusService;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Composition saveComposition(CompositionDTO compositionDTO) {
        Composition composition = modelMapper.map(compositionDTO, Composition.class);
        return getCompositionSaved(compositionDTO, composition);
    }

    private Composition getCompositionSaved(CompositionDTO compositionDTO, Composition composition) {
        updateForeignKeyComposition(compositionDTO, composition);
        return compositionRepository.save(composition);
    }

    private void updateForeignKeyComposition(CompositionDTO compositionDTO, Composition composition) {
        if (compositionDTO.getProduitId() != null)
            composition.setProduit(produitService.findProduitById(compositionDTO.getProduitId()));

        if (compositionDTO.getMenuId() != null)
            composition.setMenus(menusService.findMenusById(compositionDTO.getMenuId()));
    }

    @Override
    public Composition updateComposition(Long id, CompositionDTO compositionDTO) {
        Composition compoToUpdate = compositionRepository.findByIdComposition(id);

        if (compoToUpdate == null)
            throw new ApiRequestException("Composition non disponible");

        Composition composition = modelMapper.map(compositionDTO, Composition.class);

        composition.setId(compoToUpdate.getId());

        // update produit, menu
        updateForeignKeyComposition(compositionDTO, composition);

        return compositionRepository.save(composition);
    }

    @Override
    public Composition findCompositionById(Long id) {
        Composition composition = compositionRepository.findByIdComposition(id);
        if (composition==null)
            throw new ApiRequestException("Composition non disponible");
        return compositionRepository.findByIdComposition(id);
    }

    @Override
    public ResponseEntity<ResponseMessage> listCompositions() {
        if (compositionRepository.count()==0)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "Pas de composition disponible", null), HttpStatus.OK);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Liste des compositions", compositionRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public void deleteCompositionById(Long id) {
        Composition composition = compositionRepository.findByIdComposition(id);
        if (composition==null){
            throw new ApiRequestException("Composition non disponible");
        }else{
            compositionRepository.deleteById(id);
        }
    }

    @Override
    public List<Composition> getCompositionByProduit(Long id) {
        return compositionRepository.findCompositionByProduit(id);
    }

    @Override
    public List<Composition> getCompositionByMenu(Long id) {
        return compositionRepository.findCompositionByMenu(id);
    }
}
