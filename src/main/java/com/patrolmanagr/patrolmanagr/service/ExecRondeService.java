package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import com.patrolmanagr.patrolmanagr.dto.ExecRondeDTO;
import com.patrolmanagr.patrolmanagr.entity.Exec_ronde;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ExecRondeService {

    Exec_ronde saveExecRonde(ExecRondeDTO execRondeDTO);

    Exec_ronde updateExecRonde(Long id, ExecRondeDTO execRondeDTO);

    Exec_ronde findExecRondeById(Long id);

    List<Exec_ronde> listExecRonde();

    List<Exec_ronde> findExecRondeByProgRondeId(Long progRondeId);

    List<Exec_ronde> findExecRondeByRefRondeId(Long refRondeId);

    List<Exec_ronde> findExecRondeBySiteId(Long siteId);

    List<Exec_ronde> findExecRondeByExecDate(LocalDate execDate);

    List<Exec_ronde> findExecRondeByStatus(Status_exec_Ronde status);

    List<Exec_ronde> findExecRondeBySiteIdAndExecDate(Long siteId, LocalDate execDate);

    List<Exec_ronde> findExecRondeByExecDateAndStatus(LocalDate execDate, Status_exec_Ronde status);

    List<Exec_ronde> findExecRondeBySiteIdAndExecDateAndStatus(Long siteId, LocalDate execDate, Status_exec_Ronde status);

    List<Exec_ronde> findExecRondeByPlannedStartAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    void deleteExecRondeById(Long id);

    Exec_ronde updateExecRondeStatus(Long id, Status_exec_Ronde status, BigDecimal completionRate);

    Exec_ronde startExecRonde(Long id);

    Exec_ronde endExecRonde(Long id, BigDecimal completionRate);

    Exec_ronde updateLastEvent(Long id);
}