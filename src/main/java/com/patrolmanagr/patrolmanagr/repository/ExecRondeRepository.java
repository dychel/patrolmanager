package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.Exec_ronde;
import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExecRondeRepository extends JpaRepository<Exec_ronde, Long> {

    @Query("SELECT e FROM Exec_ronde e WHERE e.id = :id")
    Exec_ronde findByIdExecRonde(@Param("id") Long id);

    List<Exec_ronde> findByRefRondeId(Long refRondeId);

    List<Exec_ronde> findBySiteId(Long siteId);

    List<Exec_ronde> findByExecDate(LocalDate execDate);

    List<Exec_ronde> findByStatus(Status_exec_Ronde status);

    List<Exec_ronde> findByJobRunId(Long jobRunId);

    @Query("SELECT e FROM Exec_ronde e WHERE e.site.id = :siteId AND e.execDate = :execDate")
    List<Exec_ronde> findBySiteIdAndExecDate(@Param("siteId") Long siteId, @Param("execDate") LocalDate execDate);

    @Query("SELECT e FROM Exec_ronde e WHERE e.site.id = :siteId AND e.execDate BETWEEN :startDate AND :endDate")
    List<Exec_ronde> findBySiteIdAndDateRange(@Param("siteId") Long siteId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Exec_ronde e WHERE e.execDate = :execDate AND e.status = :status")
    List<Exec_ronde> findByExecDateAndStatus(@Param("execDate") LocalDate execDate,
                                             @Param("status") Status_exec_Ronde status);

    @Query("SELECT e FROM Exec_ronde e WHERE e.site.id = :siteId AND e.execDate = :execDate AND e.status = :status")
    List<Exec_ronde> findBySiteIdAndExecDateAndStatus(@Param("siteId") Long siteId,
                                                      @Param("execDate") LocalDate execDate,
                                                      @Param("status") Status_exec_Ronde status);

    @Query("SELECT e FROM Exec_ronde e WHERE e.plannedStartAt BETWEEN :startDate AND :endDate")
    List<Exec_ronde> findByPlannedStartAtBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(e) FROM Exec_ronde e WHERE e.jobRun.id = :jobRunId")
    Long countByJobRunId(@Param("jobRunId") Long jobRunId);

    @Query("SELECT e FROM Exec_ronde e WHERE e.refRonde.id = :rondeId AND e.execDate = :execDate")
    List<Exec_ronde> findByRondeIdAndDate(@Param("rondeId") Long rondeId, @Param("execDate") LocalDate execDate);

    @Query("SELECT COUNT(e) FROM Exec_ronde e WHERE e.site.id = :siteId AND e.status = :status")
    Long countBySiteIdAndStatus(@Param("siteId") Long siteId, @Param("status") Status_exec_Ronde status);

    // Méthode pour vérifier l'existence (supprimée car on n'utilise plus ProgRonde)
    // boolean existsByProgRondeAndPlannedStartAt(Long progRondeId, LocalDateTime plannedStartAt);
}