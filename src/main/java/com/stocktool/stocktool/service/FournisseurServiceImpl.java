package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.FournisseurDTO;
import com.stocktool.stocktool.entity.Categorie;
import com.stocktool.stocktool.entity.Fournisseur;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.FournisseurRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FournisseurServiceImpl implements FournisseurService{

    @Autowired
    FournisseurRepository fournisseurRepository;
    @Override
    public Fournisseur saveFournisseur(FournisseurDTO fournisseurDTO) {
        Fournisseur fournisseur = fournisseurDTO.toFournisseur();
        if (fournisseur==null)
            throw new ApiRequestException("Saving failled");
        return fournisseurRepository.save(fournisseur);
    }

    @Override
    public Fournisseur updateFournisseur(Long id, FournisseurDTO fournisseurDTO) {
        Fournisseur fournisseur_obj = fournisseurDTO.toFournisseur();
        Optional<Fournisseur> cat = fournisseurRepository.findById(id);
        if (cat.isPresent()){
            Fournisseur fournisseur_update = cat.get();
            fournisseur_obj.setId(fournisseur_update.getId());
            return fournisseurRepository.save(fournisseur_obj);
        }else {
            throw  new ApiRequestException("Oops! something bad appeneed");
        }
    }

    @Override
    public Fournisseur findFournisseurById(Long id) {
        Fournisseur fournisseur = fournisseurRepository.findByIdFournisseur(id);
        if (fournisseur==null)
            throw new ApiRequestException("no found");
        return fournisseurRepository.findByIdFournisseur(id);
    }

    @Override
    public ResponseEntity<ResponseMessage> listofFournisseur() {
        if (fournisseurRepository.count()==0)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "no data!", null), HttpStatus.OK);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "liste des fournisseur", fournisseurRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public void deleteFournisseurById(Long id) {
        Fournisseur fournisseur = fournisseurRepository.findByIdFournisseur(id);
        if (fournisseur==null){
            throw new ApiRequestException("Fournisseur not found");
        }else{
            fournisseurRepository.deleteById(id);
        }
    }
}
