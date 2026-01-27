package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.prog_ronde;
import com.patrolmanagr.patrolmanagr.config.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgRondeRepository extends JpaRepository<prog_ronde, Long> {

    @Query("SELECT p FROM prog_ronde p WHERE p.id = :id")
    prog_ronde findByIdProgRonde(@Param("id") Long id);

    @Query("SELECT p FROM prog_ronde p WHERE p.ref_ronde_id.id = :rondeId")
    List<prog_ronde> findByRondeId(@Param("rondeId") Long rondeId);

    @Query("SELECT p FROM prog_ronde p WHERE p.ref_site_id.id = :siteId")
    List<prog_ronde> findBySiteId(@Param("siteId") Long siteId);

    @Query("SELECT p FROM prog_ronde p WHERE p.assigned_agent_id.id = :userId")
    List<prog_ronde> findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM prog_ronde p WHERE p.status = :status")
    List<prog_ronde> findByStatus(@Param("status") Status status);

    @Query("SELECT p FROM prog_ronde p WHERE p.assigned_rondier_terminal_id.id = :terminalId")
    List<prog_ronde> findByTerminalId(@Param("terminalId") Long terminalId);
}