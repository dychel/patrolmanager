package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.exec_ronde_pastille;
import com.patrolmanagr.patrolmanagr.config.Status_ronde_pastille;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExecRondePastilleRepository extends JpaRepository<exec_ronde_pastille, Long> {

    @Query("SELECT e FROM exec_ronde_pastille e WHERE e.id = :id")
    exec_ronde_pastille findByIdExecRondePastille(@Param("id") Long id);

    @Query("SELECT e FROM exec_ronde_pastille e WHERE e.execRonde.id = :execRondeId")
    List<exec_ronde_pastille> findByExecRondeId(@Param("execRondeId") Long execRondeId);

    @Query("SELECT e FROM exec_ronde_pastille e WHERE e.pastille.id = :pastilleId")
    List<exec_ronde_pastille> findByPastilleId(@Param("pastilleId") Long pastilleId);

    @Query("SELECT e FROM exec_ronde_pastille e WHERE e.execRonde.id = :execRondeId AND e.seqNo = :seqNo")
    exec_ronde_pastille findByExecRondeIdAndSeqNo(@Param("execRondeId") Long execRondeId, @Param("seqNo") Integer seqNo);

    @Query("SELECT e FROM exec_ronde_pastille e WHERE e.execRonde.id = :execRondeId ORDER BY e.seqNo ASC")
    List<exec_ronde_pastille> findByExecRondeIdOrderBySeqNo(@Param("execRondeId") Long execRondeId);

    @Query("SELECT e FROM exec_ronde_pastille e WHERE e.status = :status")
    List<exec_ronde_pastille> findByStatus(@Param("status") Status_ronde_pastille status);

    @Query("SELECT e FROM exec_ronde_pastille e WHERE e.execRonde.id = :execRondeId AND e.status = :status")
    List<exec_ronde_pastille> findByExecRondeIdAndStatus(@Param("execRondeId") Long execRondeId, @Param("status") Status_ronde_pastille status);

    @Query("SELECT e FROM exec_ronde_pastille e WHERE e.scannedAt BETWEEN :startDate AND :endDate")
    List<exec_ronde_pastille> findByScannedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM exec_ronde_pastille e WHERE e.pointageId = :pointageId")
    List<exec_ronde_pastille> findByPointageId(@Param("pointageId") Long pointageId);

    @Query("SELECT e FROM exec_ronde_pastille e WHERE e.execRonde.id = :execRondeId AND e.pastille.id = :pastilleId")
    exec_ronde_pastille findByExecRondeIdAndPastilleId(@Param("execRondeId") Long execRondeId, @Param("pastilleId") Long pastilleId);

    @Query("SELECT COUNT(e) > 0 FROM exec_ronde_pastille e WHERE e.execRonde.id = :execRondeId AND e.seqNo = :seqNo")
    boolean existsByExecRondeIdAndSeqNo(@Param("execRondeId") Long execRondeId, @Param("seqNo") Integer seqNo);

    @Query("SELECT COUNT(e) FROM exec_ronde_pastille e WHERE e.execRonde.id = :execRondeId AND e.status = :status")
    Long countByExecRondeIdAndStatus(@Param("execRondeId") Long execRondeId, @Param("status") Status_ronde_pastille status);
}