package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.Ref_pastilleDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import java.util.List;

public interface RefPastilleService {
    Ref_pastille savePastille(Ref_pastilleDTO refPastilleDTO);
    Ref_pastille updatePastille(Long id, Ref_pastilleDTO refPastilleDTO);
    Ref_pastille findPastilleById(Long id);
    List<Ref_pastille> listRef_pastille();
    Ref_pastille findPastilleByIdSite(Long id);
    Ref_pastille findPastilleByIdSecteur(Long id);
    void deletePastilleById(Long id);
}
