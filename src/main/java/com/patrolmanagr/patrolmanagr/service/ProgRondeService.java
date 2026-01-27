package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.ProgRondeDTO;
import com.patrolmanagr.patrolmanagr.entity.prog_ronde;
import com.patrolmanagr.patrolmanagr.config.Status;

import java.util.List;

public interface ProgRondeService {

    prog_ronde saveProgRonde(ProgRondeDTO progRondeDTO);

    prog_ronde updateProgRonde(Long id, ProgRondeDTO progRondeDTO);

    prog_ronde findProgRondeById(Long id);

    List<prog_ronde> listProgRonde();

    List<prog_ronde> findProgRondeByRondeId(Long rondeId);

    List<prog_ronde> findProgRondeBySiteId(Long siteId);

    List<prog_ronde> findProgRondeByUserId(Long userId);

    List<prog_ronde> findProgRondeByStatus(Status status);

    List<prog_ronde> findProgRondeByTerminalId(Long terminalId);

    void deleteProgRondeById(Long id);
}