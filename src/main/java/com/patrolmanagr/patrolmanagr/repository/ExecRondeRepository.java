package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import com.patrolmanagr.patrolmanagr.entity.Exec_ronde;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExecRondeRepository extends JpaRepository<Exec_ronde, Long> {

    @Query("SELECT e FROM Exec_ronde e WHERE e.id = :id")
    Exec_ronde findByIdExecRonde(@Param("id") Long id);

    @Query("SELECT e FROM Exec_ronde e WHERE e.progRonde.id = :progRondeId")
    List<Exec_ronde> findByProgRondeId(@Param("progRondeId") Long progRondeId);

    @Query("SELECT e FROM Exec_ronde e WHERE e.refRonde.id = :refRondeId")
    List<Exec_ronde> findByRefRondeId(@Param("refRondeId") Long refRondeId);

    @Query("SELECT e FROM Exec_ronde e WHERE e.site.id = :siteId")
    List<Exec_ronde> findBySiteId(@Param("siteId") Long siteId);

    @Query("SELECT e FROM Exec_ronde e WHERE e.execDate = :execDate")
    List<Exec_ronde> findByExecDate(@Param("execDate") LocalDate execDate);

    @Query("SELECT e FROM Exec_ronde e WHERE e.status = :status")
    List<Exec_ronde> findByStatus(@Param("status") Status_exec_Ronde status);

    @Query("SELECT e FROM Exec_ronde e WHERE e.site.id = :siteId AND e.execDate = :execDate")
    List<Exec_ronde> findBySiteIdAndExecDate(@Param("siteId") Long siteId, @Param("execDate") LocalDate execDate);

    @Query("SELECT e FROM Exec_ronde e WHERE e.site.id = :siteId AND e.execDate = :execDate AND e.status = :status")
    List<Exec_ronde> findBySiteIdAndExecDateAndStatus(@Param("siteId") Long siteId,
                                                      @Param("execDate") LocalDate execDate,
                                                      @Param("status") Status_exec_Ronde status);

    @Query("SELECT e FROM Exec_ronde e WHERE e.execDate = :execDate AND e.status = :status")
    List<Exec_ronde> findByExecDateAndStatus(@Param("execDate") LocalDate execDate,
                                             @Param("status") Status_exec_Ronde status);

    @Query("SELECT e FROM Exec_ronde e WHERE e.plannedStartAt BETWEEN :startDate AND :endDate")
    List<Exec_ronde> findByPlannedStartAtBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Exec_ronde e WHERE e.progRonde.id = :progRondeId AND e.plannedStartAt = :plannedStartAt")
    Exec_ronde findByProgRondeIdAndPlannedStartAt(@Param("progRondeId") Long progRondeId,
                                                  @Param("plannedStartAt") LocalDateTime plannedStartAt);

    @Query("SELECT e FROM Exec_ronde e WHERE e.completionRate IS NOT NULL ORDER BY e.completionRate DESC")
    List<Exec_ronde> findAllWithCompletionRate();

    @Query("SELECT e FROM Exec_ronde e WHERE e.execDate = :execDate AND e.site.id = :siteId")
    List<Exec_ronde> findByDateAndSite(@Param("execDate") LocalDate execDate, @Param("siteId") Long siteId);

    @Query("SELECT COUNT(e) > 0 FROM Exec_ronde e WHERE e.progRonde.id = :progRondeId AND e.plannedStartAt = :plannedStartAt")
    boolean existsByProgRondeAndPlannedStartAt(@Param("progRondeId") Long progRondeId,
                                               @Param("plannedStartAt") LocalDateTime plannedStartAt);
}