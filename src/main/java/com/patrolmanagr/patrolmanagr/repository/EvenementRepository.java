package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.config.EvenementSeverity;
import com.patrolmanagr.patrolmanagr.entity.Evenement;
import com.patrolmanagr.patrolmanagr.config.EvenementType;
import com.patrolmanagr.patrolmanagr.config.EvenementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Long> {

    List<Evenement> findByExecRondeId(Long execRondeId);

    List<Evenement> findBySiteId(Long siteId);

    List<Evenement> findByRondeId(Long rondeId);

    List<Evenement> findByJobRunId(Long jobRunId);

    List<Evenement> findByType(EvenementType type);

    List<Evenement> findBySeverity(EvenementSeverity severity);

    List<Evenement> findByStatus(EvenementStatus status);

    List<Evenement> findByDetectedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT e FROM Evenement e WHERE e.execRondePastille.id = :pastilleId")
    List<Evenement> findByExecRondePastilleId(@Param("pastilleId") Long pastilleId);

    @Query("SELECT e FROM Evenement e WHERE e.pointageId = :pointageId")
    List<Evenement> findByPointageId(@Param("pointageId") Long pointageId);

    @Query("SELECT e FROM Evenement e WHERE e.execRonde.id = :execRondeId AND e.severity = :severity")
    List<Evenement> findByExecRondeIdAndSeverity(@Param("execRondeId") Long execRondeId,
                                                 @Param("severity") EvenementSeverity severity);

    @Query("SELECT COUNT(e) FROM Evenement e WHERE e.siteId = :siteId AND e.detectedAt >= :startDate")
    Long countRecentEventsBySite(@Param("siteId") Long siteId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT e FROM Evenement e WHERE e.severity = :severity AND e.status = :status")
    List<Evenement> findBySeverityAndStatus(@Param("severity") EvenementSeverity severity,
                                            @Param("status") EvenementStatus status);

    @Query("SELECT COUNT(e) FROM Evenement e WHERE e.execRonde.id = :execRondeId AND e.type = :type")
    Long countByExecRondeIdAndType(@Param("execRondeId") Long execRondeId,
                                   @Param("type") EvenementType type);
}