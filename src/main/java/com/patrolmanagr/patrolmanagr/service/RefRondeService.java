package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.Ref_rondeDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;

import java.util.List;

public interface RefRondeService {

    Ref_ronde saveRonde(Ref_rondeDTO ref_rondeDTO);
    Ref_ronde updateRonde(Long id, Ref_rondeDTO ref_rondeDTO);
    Ref_ronde findRondeById(Long id);
    List<Ref_ronde> listRonde();
    List<Ref_ronde> findRondeByIdSite(Long id);
    void deleteRondeById(Long id);
}
