package com.patrolmanagr.patrolmanagr.repository;
import com.patrolmanagr.patrolmanagr.entity.Prog_ronde;
import com.patrolmanagr.patrolmanagr.config.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgRondeRepository extends JpaRepository<Prog_ronde, Long> {

    @Query("SELECT p FROM Prog_ronde p WHERE p.id = :id")
    Prog_ronde findByIdProgRonde(@Param("id") Long id);

    @Query("SELECT p FROM Prog_ronde p WHERE p.ref_ronde.id = :rondeId")
    List<Prog_ronde> findByRondeId(@Param("rondeId") Long rondeId);

    @Query("SELECT p FROM Prog_ronde p WHERE p.ref_site.id = :siteId")
    List<Prog_ronde> findBySiteId(@Param("siteId") Long siteId);

    @Query("SELECT p FROM Prog_ronde p WHERE p.assigned_agent_id.id = :userId")
    List<Prog_ronde> findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Prog_ronde p WHERE p.status = :status")
    List<Prog_ronde> findByStatus(@Param("status") Status status);

    @Query("SELECT p FROM Prog_ronde p WHERE p.assigned_rondier_terminal_id.id = :terminalId")
    List<Prog_ronde> findByTerminalId(@Param("terminalId") Long terminalId);
}