package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.Evenement;
import com.patrolmanagr.patrolmanagr.config.IncidentStatus;
import com.patrolmanagr.patrolmanagr.config.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Evenement, Long> {

    List<Evenement> findByExecRondeId(Long execRondeId);

    List<Evenement> findBySiteId(Long siteId);

    List<Evenement> findByRondeId(Long rondeId);

    List<Evenement> findByPastilleId(Long pastilleId);

    List<Evenement> findByType(IncidentType type);

    List<Evenement> findByStatus(IncidentStatus status);

    List<Evenement> findByAgentUserId(Long agentUserId);

    @Query("SELECT i FROM Evenement i WHERE i.detectedAt BETWEEN :startDate AND :endDate")
    List<Evenement> findByDetectedAtBetween(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM Evenement i WHERE i.siteId = :siteId AND i.detectedAt BETWEEN :startDate AND :endDate")
    List<Evenement> findBySiteIdAndDateRange(@Param("siteId") Long siteId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM Evenement i WHERE i.execRonde.id = :execRondeId AND i.type = :type")
    List<Evenement> findByExecRondeIdAndType(@Param("execRondeId") Long execRondeId,
                                             @Param("type") IncidentType type);

    @Query("SELECT COUNT(i) FROM Evenement i WHERE i.execRonde.id = :execRondeId")
    Long countByExecRondeId(@Param("execRondeId") Long execRondeId);

    @Query("SELECT i FROM Evenement i WHERE i.execRondePastille.id = :execRondePastilleId")
    List<Evenement> findByExecRondePastilleId(@Param("execRondePastilleId") Long execRondePastilleId);

    @Query("SELECT i FROM Evenement i WHERE i.pointageId = :pointageId")
    List<Evenement> findByPointageId(@Param("pointageId") Long pointageId);
}