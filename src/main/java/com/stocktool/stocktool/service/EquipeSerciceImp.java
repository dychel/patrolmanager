package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.EquipeDTO;
import com.stocktool.stocktool.entity.Equipe;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.EquipeRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class EquipeSerciceImp implements EquipeService{
    @Autowired
    EquipeRepository equipeRepository;

    @Override
    public Equipe saveEquipe(EquipeDTO equipeDTO) {
        Equipe equipe_obj = equipeDTO.toEquipe();
        if(equipe_obj==null)
            throw new ApiRequestException("Saving failled");
        return equipeRepository.save(equipe_obj);
    }

    @Override
    public Equipe updateEquipe(Long id, EquipeDTO equipeDTO) {
        Equipe equipe_obj = equipeDTO.toEquipe();
        Optional<Equipe> equip = equipeRepository.findById(id);
        if (equip.isPresent()){
            Equipe equipe_update = equip.get();
            equipe_obj.setId(equipe_update.getId());
            return equipeRepository.save(equipe_obj);
        }else {
            throw  new ApiRequestException("Oops! something bad appeneed");
        }
    }

    @Override
    public Equipe findEquipeById(Long id) {
        Equipe equipe = equipeRepository.findByIdEquipe(id);
        if (equipe==null)
            throw new ApiRequestException("equipe not found!");
        return  equipeRepository.findByIdEquipe(id);
    }

    @Override
    public ResponseEntity<ResponseMessage> listofequipe() {
        if (equipeRepository.count()==0)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "no data!", null), HttpStatus.OK);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "list of equipe", equipeRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public void deleteEquipeById(Long id) {
        Equipe equipe = equipeRepository.findByIdEquipe(id);
        if (equipe==null){
            throw new ApiRequestException("Equipe not found");
        }else{
            equipeRepository.deleteById(id);
        }
    }
}
