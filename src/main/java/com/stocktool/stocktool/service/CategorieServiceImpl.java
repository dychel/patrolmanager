package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.CategorieDTO;
import com.stocktool.stocktool.entity.Categorie;
import com.stocktool.stocktool.entity.Equipe;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.CategorieRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CategorieServiceImpl implements CategorieService{

    @Autowired
    CategorieRepository categorieRepository;
    @Override
    public Categorie saveCategorie(CategorieDTO categorieDTO) {
        Categorie categorie = categorieDTO.toCategorie();
        if (categorie==null)
            throw new ApiRequestException("Saving failled");
        return categorieRepository.save(categorie);
    }

    @Override
    public Categorie updateCategorie(Long id, CategorieDTO categorieDTO) {
        Categorie categorie_obj = categorieDTO.toCategorie();
        Optional<Categorie> cat = categorieRepository.findById(id);
        if (cat.isPresent()){
            Categorie categorie_update = cat.get();
            categorie_obj.setId(categorie_update.getId());
            return categorieRepository.save(categorie_obj);
        }else {
            throw  new ApiRequestException("Oops! something bad appeneed");
        }
    }

    @Override
    public Categorie findCategorieById(Long id) {
        Categorie categorie = categorieRepository.findByIdCategorie(id);
        if (categorie==null)
            throw new ApiRequestException("no found");
        return categorieRepository.findByIdCategorie(id);
    }

    @Override
    public ResponseEntity<ResponseMessage> listofCategorie() {
        if (categorieRepository.count()==0)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "no data!", null), HttpStatus.OK);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "liste des categorie", categorieRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public void deleteCategorieById(Long id) {
        Categorie categorie = categorieRepository.findByIdCategorie(id);
        if (categorie==null){
            throw new ApiRequestException("Categorie not found");
        }else{
            categorieRepository.deleteById(id);
        }
    }
}
