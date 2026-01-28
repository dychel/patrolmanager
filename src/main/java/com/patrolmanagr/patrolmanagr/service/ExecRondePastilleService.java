package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.ExecRondePastilleDTO;
import com.patrolmanagr.patrolmanagr.entity.Exec_ronde_pastille;
import com.patrolmanagr.patrolmanagr.config.Status_ronde_pastille;

import java.time.LocalDateTime;
import java.util.List;

public interface ExecRondePastilleService {

    Exec_ronde_pastille saveExecRondePastille(ExecRondePastilleDTO execRondePastilleDTO);

    Exec_ronde_pastille updateExecRondePastille(Long id, ExecRondePastilleDTO execRondePastilleDTO);

    Exec_ronde_pastille findExecRondePastilleById(Long id);

    List<Exec_ronde_pastille> listExecRondePastille();

    List<Exec_ronde_pastille> findExecRondePastilleByExecRondeId(Long execRondeId);

    List<Exec_ronde_pastille> findExecRondePastilleByPastilleId(Long pastilleId);

    Exec_ronde_pastille findExecRondePastilleByExecRondeIdAndSeqNo(Long execRondeId, Integer seqNo);

    List<Exec_ronde_pastille> findExecRondePastilleByExecRondeIdOrderBySeqNo(Long execRondeId);

    List<Exec_ronde_pastille> findExecRondePastilleByStatus(Status_ronde_pastille status);

    List<Exec_ronde_pastille> findExecRondePastilleByExecRondeIdAndStatus(Long execRondeId, Status_ronde_pastille status);

    List<Exec_ronde_pastille> findExecRondePastilleByScannedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Exec_ronde_pastille> findExecRondePastilleByPointageId(Long pointageId);

    void deleteExecRondePastilleById(Long id);

    Exec_ronde_pastille markAsDone(Long id, LocalDateTime scannedAt, Integer actualTravelSec, String notes);

    Exec_ronde_pastille markAsMissed(Long id, String notes);

    Exec_ronde_pastille updatePointage(Long id, Long pointageId);

    List<Exec_ronde_pastille> initializeFromRonde(Long execRondeId);

    Long countByExecRondeIdAndStatus(Long execRondeId, Status_ronde_pastille status);
}