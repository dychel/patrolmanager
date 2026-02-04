package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.Incident;
import com.patrolmanagr.patrolmanagr.config.IncidentStatus;
import com.patrolmanagr.patrolmanagr.config.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByExecRondeId(Long execRondeId);

    List<Incident> findBySiteId(Long siteId);

    List<Incident> findByRondeId(Long rondeId);

    List<Incident> findByPastilleId(Long pastilleId);

    List<Incident> findByType(IncidentType type);

    List<Incident> findByStatus(IncidentStatus status);

    List<Incident> findByAgentUserId(Long agentUserId);

    @Query("SELECT i FROM Incident i WHERE i.detectedAt BETWEEN :startDate AND :endDate")
    List<Incident> findByDetectedAtBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM Incident i WHERE i.siteId = :siteId AND i.detectedAt BETWEEN :startDate AND :endDate")
    List<Incident> findBySiteIdAndDateRange(@Param("siteId") Long siteId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM Incident i WHERE i.execRonde.id = :execRondeId AND i.type = :type")
    List<Incident> findByExecRondeIdAndType(@Param("execRondeId") Long execRondeId,
                                            @Param("type") IncidentType type);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.execRonde.id = :execRondeId")
    Long countByExecRondeId(@Param("execRondeId") Long execRondeId);

    @Query("SELECT i FROM Incident i WHERE i.execRondePastille.id = :execRondePastilleId")
    List<Incident> findByExecRondePastilleId(@Param("execRondePastilleId") Long execRondePastilleId);

    @Query("SELECT i FROM Incident i WHERE i.pointageId = :pointageId")
    List<Incident> findByPointageId(@Param("pointageId") Long pointageId);
}