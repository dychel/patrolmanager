package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import com.patrolmanagr.patrolmanagr.entity.exec_ronde;
import com.patrolmanagr.patrolmanagr.config.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExecRondeRepository extends JpaRepository<exec_ronde, Long> {

    @Query("SELECT e FROM exec_ronde e WHERE e.id = :id")
    exec_ronde findByIdExecRonde(@Param("id") Long id);

    @Query("SELECT e FROM exec_ronde e WHERE e.progRonde.id = :progRondeId")
    List<exec_ronde> findByProgRondeId(@Param("progRondeId") Long progRondeId);

    @Query("SELECT e FROM exec_ronde e WHERE e.refRonde.id = :refRondeId")
    List<exec_ronde> findByRefRondeId(@Param("refRondeId") Long refRondeId);

    @Query("SELECT e FROM exec_ronde e WHERE e.site.id = :siteId")
    List<exec_ronde> findBySiteId(@Param("siteId") Long siteId);

    @Query("SELECT e FROM exec_ronde e WHERE e.execDate = :execDate")
    List<exec_ronde> findByExecDate(@Param("execDate") LocalDate execDate);

    @Query("SELECT e FROM exec_ronde e WHERE e.status = :status")
    List<exec_ronde> findByStatus(@Param("status") Status_exec_Ronde status);

    @Query("SELECT e FROM exec_ronde e WHERE e.site.id = :siteId AND e.execDate = :execDate")
    List<exec_ronde> findBySiteIdAndExecDate(@Param("siteId") Long siteId, @Param("execDate") LocalDate execDate);

    @Query("SELECT e FROM exec_ronde e WHERE e.site.id = :siteId AND e.execDate = :execDate AND e.status = :status")
    List<exec_ronde> findBySiteIdAndExecDateAndStatus(@Param("siteId") Long siteId,
                                                      @Param("execDate") LocalDate execDate,
                                                      @Param("status") Status_exec_Ronde status);

    @Query("SELECT e FROM exec_ronde e WHERE e.execDate = :execDate AND e.status = :status")
    List<exec_ronde> findByExecDateAndStatus(@Param("execDate") LocalDate execDate,
                                             @Param("status") Status_exec_Ronde status);

    @Query("SELECT e FROM exec_ronde e WHERE e.plannedStartAt BETWEEN :startDate AND :endDate")
    List<exec_ronde> findByPlannedStartAtBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM exec_ronde e WHERE e.progRonde.id = :progRondeId AND e.plannedStartAt = :plannedStartAt")
    exec_ronde findByProgRondeIdAndPlannedStartAt(@Param("progRondeId") Long progRondeId,
                                                  @Param("plannedStartAt") LocalDateTime plannedStartAt);

    @Query("SELECT e FROM exec_ronde e WHERE e.completionRate IS NOT NULL ORDER BY e.completionRate DESC")
    List<exec_ronde> findAllWithCompletionRate();

    @Query("SELECT e FROM exec_ronde e WHERE e.execDate = :execDate AND e.site.id = :siteId")
    List<exec_ronde> findByDateAndSite(@Param("execDate") LocalDate execDate, @Param("siteId") Long siteId);

    @Query("SELECT COUNT(e) > 0 FROM exec_ronde e WHERE e.progRonde.id = :progRondeId AND e.plannedStartAt = :plannedStartAt")
    boolean existsByProgRondeAndPlannedStartAt(@Param("progRondeId") Long progRondeId,
                                               @Param("plannedStartAt") LocalDateTime plannedStartAt);
}