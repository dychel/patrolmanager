package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import com.patrolmanagr.patrolmanagr.dto.ExecRondeDTO;
import com.patrolmanagr.patrolmanagr.entity.exec_ronde;
import com.patrolmanagr.patrolmanagr.config.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ExecRondeService {

    exec_ronde saveExecRonde(ExecRondeDTO execRondeDTO);

    exec_ronde updateExecRonde(Long id, ExecRondeDTO execRondeDTO);

    exec_ronde findExecRondeById(Long id);

    List<exec_ronde> listExecRonde();

    List<exec_ronde> findExecRondeByProgRondeId(Long progRondeId);

    List<exec_ronde> findExecRondeByRefRondeId(Long refRondeId);

    List<exec_ronde> findExecRondeBySiteId(Long siteId);

    List<exec_ronde> findExecRondeByExecDate(LocalDate execDate);

    List<exec_ronde> findExecRondeByStatus(Status_exec_Ronde status);

    List<exec_ronde> findExecRondeBySiteIdAndExecDate(Long siteId, LocalDate execDate);

    List<exec_ronde> findExecRondeByExecDateAndStatus(LocalDate execDate, Status_exec_Ronde status);

    List<exec_ronde> findExecRondeBySiteIdAndExecDateAndStatus(Long siteId, LocalDate execDate, Status_exec_Ronde status);

    List<exec_ronde> findExecRondeByPlannedStartAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    void deleteExecRondeById(Long id);

    exec_ronde updateExecRondeStatus(Long id, Status_exec_Ronde status, BigDecimal completionRate);

    exec_ronde startExecRonde(Long id);

    exec_ronde endExecRonde(Long id, BigDecimal completionRate);

    exec_ronde updateLastEvent(Long id);
}