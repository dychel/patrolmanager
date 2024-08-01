package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.UniteDTO;
import com.stocktool.stocktool.entity.Unite;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.UniteRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UniteServiceImpl implements UniteService{

    @Autowired
    UniteRepository uniteRepository;
    @Override
    public Unite saveUnite(UniteDTO uniteDTO) {
        Unite unite = uniteDTO.toUnite();
        if (unite==null)
            throw new ApiRequestException("Saving failled");
        return uniteRepository.save(unite);
    }

    @Override
    public Unite updateUnite(Long id, UniteDTO uniteDTO) {
        Unite unite_obj = uniteDTO.toUnite();
        Optional<Unite> unit = uniteRepository.findById(id);
        if (unit.isPresent()){
            Unite unite_update = unit.get();
            unite_obj.setId(unite_update.getId());
            return uniteRepository.save(unite_obj);
        }else {
            throw  new ApiRequestException("Oops! something bad appeneed");
        }
    }

    @Override
    public Unite findUniteById(Long id) {
        Unite unite = uniteRepository.findByIdUnite(id);
        if (unite==null)
            throw new ApiRequestException("Not found");
        return uniteRepository.findByIdUnite(id);
    }

    @Override
    public ResponseEntity<ResponseMessage> listofUnite() {
        if (uniteRepository.count()==0)
           return new ResponseEntity<ResponseMessage>(new ResponseMessage("erro", "no data", null), HttpStatus.OK);
    return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Liste des unites", uniteRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public void deleteUniteById(Long id) {
        Unite unite = uniteRepository.findByIdUnite(id);
        if (unite==null){
            throw new ApiRequestException("Unite not found");
        }else{
            uniteRepository.deleteById(id);
        }
    }
}
