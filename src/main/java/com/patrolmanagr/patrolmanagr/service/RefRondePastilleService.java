package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.RefRondePastilleDTO;
import com.patrolmanagr.patrolmanagr.dto.RondePastilleOrderDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde_pastille;
import java.util.List;

public interface RefRondePastilleService {
    Ref_ronde_pastille saveRondePastille(RefRondePastilleDTO refRondePastilleDTO);
    Ref_ronde_pastille updateRondePastille(Long id, RefRondePastilleDTO refRondePastilleDTO);
    void updatePastilleOrder(RondePastilleOrderDTO orderDTO);
    List<Ref_ronde_pastille> getPastillesForRondeWithDetails(Long rondeId);
    Ref_ronde_pastille findRondePastilleById(Long id);
    List<Ref_ronde_pastille> listRondePastille();
    List<Ref_pastille> listPastille();
    List<Ref_ronde_pastille> findRondePastilleByRondeId(Long rondeId);
    List<Ref_ronde_pastille> findRondePastilleByPastilleId(Long pastilleId);
    Ref_ronde_pastille findRondePastilleByRondeIdAndSequence(Long rondeId, Integer sequence);
    void deleteRondePastilleById(Long id);
    void deleteRondePastilleByRondeId(Long rondeId);
    List<Ref_ronde_pastille> findRondePastilleByRondeIdOrderBySequence(Long rondeId);
}