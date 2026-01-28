package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.ProgRondeDTO;
import com.patrolmanagr.patrolmanagr.entity.Prog_ronde;
import com.patrolmanagr.patrolmanagr.config.Status;

import java.util.List;

public interface ProgRondeService {

    Prog_ronde saveProgRonde(ProgRondeDTO progRondeDTO);

    Prog_ronde updateProgRonde(Long id, ProgRondeDTO progRondeDTO);

    Prog_ronde findProgRondeById(Long id);

    List<Prog_ronde> listProgRonde();

    List<Prog_ronde> findProgRondeByRondeId(Long rondeId);

    List<Prog_ronde> findProgRondeBySiteId(Long siteId);

    List<Prog_ronde> findProgRondeByUserId(Long userId);

    List<Prog_ronde> findProgRondeByStatus(Status status);

    List<Prog_ronde> findProgRondeByTerminalId(Long terminalId);

    void deleteProgRondeById(Long id);
}