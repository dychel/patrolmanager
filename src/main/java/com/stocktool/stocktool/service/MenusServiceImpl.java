package com.stocktool.stocktool.service;
import com.stocktool.stocktool.dto.MenusDTO;
import com.stocktool.stocktool.dto.ProduitDTO;
import com.stocktool.stocktool.entity.Menus;
import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.MenusRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenusServiceImpl implements MenusService{

    @Autowired
    private MenusRepository menusRepository;
    @Autowired
    private ProduitService produitService;

    @Autowired
    private ModelMapper modelMapper;
    @Override
    public Menus saveMenus(MenusDTO menusDTO) {
        Menus menus = modelMapper.map(menusDTO, Menus.class);
        return getMenusSaved( menus);
    }

    @Override
    public Menus updateMenus(Long id, MenusDTO menusDTO) {
        Menus menuToUpdate = menusRepository.findByIdMenus(id);

        if (menuToUpdate == null)
            throw new ApiRequestException("Menu non disponible");

        Menus menus = modelMapper.map(menusDTO, Menus.class);

        menus.setId(menuToUpdate.getId());

        // MAJ des tables unit, brand et Catégories liées à Product
//        updateForeignKeyMenu(menusDTO, menus);

        return menusRepository.save(menus);
    }

    private Menus getMenusSaved(Menus menus) {
//        MAJ des tables unit, brand et Catégories liées à Product
        //updateForeignKeyMenu(menusDTO, menus);

//        Sauvegarde des images
        //  gestionImages(productDTO, product);

//        Sauvegarde des brochures
        //   gestionBrochures(productDTO, product);

        return menusRepository.save(menus);
    }
//    private void updateForeignKeyMenu(MenusDTO menusDTO, Menus menus) {
//        if (menusDTO.getProduitId() != null)
//            menus.setProduit(produitService.findProduitById(menusDTO.getProduitId()));
//    }

    @Override
    public Menus findMenusById(Long id) {
        Menus menus = menusRepository.findByIdMenus(id);
        if (menus==null)
            throw new ApiRequestException("Produit non disponible");
        return menusRepository.findByIdMenus(id);
    }

    @Override
    public ResponseEntity<ResponseMessage> listMenus() {
        if (menusRepository.count()==0)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "Pas de menu disponible", null), HttpStatus.OK);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Liste des Menus", menusRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public void deleteMenusById(Long id) {
        Menus menus = menusRepository.findByIdMenus(id);
        if (menus==null){
            throw new ApiRequestException("Menu non disponible");
        }else{
            menusRepository.deleteById(id);
        }
    }

//    @Override
//    public List<Menus> getMenusByProduit(Long id) {
//        return menusRepository.findMenusByProduit(id);
//    }
}
