package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.Ref_rondeDTO;
import com.patrolmanagr.patrolmanagr.dto.Ref_terminalDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
import com.patrolmanagr.patrolmanagr.entity.Ref_terminal;

import java.util.List;

public interface RefRondeSercice {

    Ref_ronde saveRonde(Ref_rondeDTO ref_rondeDTO);
    Ref_ronde updateRonde(Long id, Ref_rondeDTO ref_rondeDTO);
    Ref_ronde findRondeById(Long id);
    List<Ref_ronde> listRonde();
    Ref_ronde findRondeByIdSite(Long id);
    void deleteRondeById(Long id);
}
