package com.stocktool.stocktool.service;
import com.stocktool.stocktool.dto.MarqueDTO;
import com.stocktool.stocktool.entity.Equipe;
import com.stocktool.stocktool.entity.Marque;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.MarqueRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MarqueServiceImpl implements MarqueService{

    @Autowired
    MarqueRepository marqueRepository;
    @Override
    public Marque saveMarque(MarqueDTO marqueDTO) {
        Marque marque = marqueDTO.toMarque();
        if (marque==null)
            throw new ApiRequestException("Saving failled");
        return marqueRepository.save(marque);
    }

    @Override
    public Marque updateMarque(Long id, MarqueDTO marqueDTO) {
        Marque marque_obj = marqueDTO.toMarque();
        Optional<Marque> marq = marqueRepository.findById(id);
        if (marq.isPresent()) {
            Marque marque_update = marq.get();
            marque_obj.setId(marque_update.getId());
            return marqueRepository.save(marque_obj);
        }else {
            throw  new ApiRequestException("Oops! something bad appeneed");
        }
    }

    @Override
    public Marque findMarqueById(Long id) {
        Marque marque = marqueRepository.findByIdMarque(id);
        if (marque==null)
            throw new ApiRequestException("Not found");
        return marqueRepository.findByIdMarque(id);
    }

    @Override
    public ResponseEntity<ResponseMessage> listofMarque() {
        if (marqueRepository.count()==0)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "no list", null), HttpStatus.OK);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "no list", marqueRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public void deleteMarqueById(Long id) {
        Marque marque = marqueRepository.findByIdMarque(id);
        if (marque==null){
            throw new ApiRequestException("Marque not found");
        }else {
            marqueRepository.deleteById(id);
        }
    }
}
