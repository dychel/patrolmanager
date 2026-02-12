package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.Fact_pointage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactPointageRepository extends JpaRepository<Fact_pointage, Long> {

    // 🔑 Récupérer le timestamp du dernier pointage (AJOUT IMPORTANT !)
    @Query("SELECT MAX(f.eventTime) FROM Fact_pointage f")
    LocalDateTime findLastPointageTime();

    // Recherche par période
    List<Fact_pointage> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    List<Fact_pointage> findByEventTimeBetween(LocalDateTime start, LocalDateTime end);

    // Recherche par site
    List<Fact_pointage> findBySiteId(Long siteId);

    List<Fact_pointage> findBySiteIdAndEventDateBetween(Long siteId, LocalDate startDate, LocalDate endDate);

    // Recherche par site et période avec LocalDateTime
    @Query("SELECT fp FROM Fact_pointage fp WHERE fp.siteId = :siteId AND fp.eventTime BETWEEN :startDate AND :endDate")
    List<Fact_pointage> findBySiteIdAndEventTimeBetween(
            @Param("siteId") Long siteId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Recherche par ronde
    List<Fact_pointage> findByRondeId(Long rondeId);

    List<Fact_pointage> findByRondeIdAndEventDateBetween(Long rondeId, LocalDate startDate, LocalDate endDate);

    // Recherche par pastille
    List<Fact_pointage> findByPastilleId(Long pastilleId);

    List<Fact_pointage> findByPastilleCodeRaw(String pastilleCodeRaw);

    // Recherche par agent
    List<Fact_pointage> findByAgentUserId(Long agentUserId);

    List<Fact_pointage> findByAgentCodeRaw(String agentCodeRaw);

    // Recherche par terminal
    List<Fact_pointage> findByTerminalId(Long terminalId);

    List<Fact_pointage> findByTerminalCodeRaw(String terminalCodeRaw);

    // Recherche par statut de traitement
    List<Fact_pointage> findByProcessedStatus(String processedStatus);

    // Recherche par source
    List<Fact_pointage> findBySourceType(String sourceType);

    List<Fact_pointage> findBySourceBatchId(Long sourceBatchId);

    // Recherche combinée
    @Query("SELECT fp FROM Fact_pointage fp WHERE " +
            "fp.siteId = :siteId AND " +
            "fp.rondeId = :rondeId AND " +
            "fp.eventDate BETWEEN :startDate AND :endDate")
    List<Fact_pointage> findPointageBySiteAndRondeAndDate(
            @Param("siteId") Long siteId,
            @Param("rondeId") Long rondeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Recherche de pointages rejetés avec raison
    List<Fact_pointage> findByProcessedStatusAndRejectionReasonIsNotNull(String processedStatus);

    // Recherche par vendor
    List<Fact_pointage> findByVendorId(Long vendorId);

    // Recherche par zone/secteur
    List<Fact_pointage> findByZoneId(Long zoneId);

    List<Fact_pointage> findBySecteurId(Long secteurId);

    // Vérification d'existence pour éviter les doublons
    @Query("SELECT COUNT(fp) > 0 FROM Fact_pointage fp WHERE " +
            "fp.eventTime = :eventTime AND " +
            "fp.pastilleCodeRaw = :pastilleCodeRaw AND " +
            "fp.terminalCodeRaw = :terminalCodeRaw")
    boolean existsByUniqueKey(
            @Param("eventTime") LocalDateTime eventTime,
            @Param("pastilleCodeRaw") String pastilleCodeRaw,
            @Param("terminalCodeRaw") String terminalCodeRaw);

    // Statistiques par jour
    @Query("SELECT fp.eventDate, COUNT(fp) FROM Fact_pointage fp " +
            "WHERE fp.eventDate BETWEEN :startDate AND :endDate " +
            "GROUP BY fp.eventDate")
    List<Object[]> countByEventDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(fp) FROM Fact_pointage fp WHERE fp.processedStatus = :status")
    long countByProcessedStatus(@Param("status") String status);

    @Query("SELECT fp FROM Fact_pointage fp ORDER BY fp.createdAt DESC LIMIT 1")
    Optional<Fact_pointage> findTopByOrderByCreatedAtDesc();

    @Query("SELECT fp.eventTime, fp.pastilleCodeRaw, fp.terminalCodeRaw, COUNT(*) as cnt " +
            "FROM Fact_pointage fp " +
            "GROUP BY fp.eventTime, fp.pastilleCodeRaw, fp.terminalCodeRaw " +
            "HAVING COUNT(*) > 1")
    List<Object[]> findPotentialDuplicates();

    // Recherche par pastille et période
    @Query("SELECT fp FROM Fact_pointage fp WHERE fp.pastilleId = :pastilleId AND fp.eventTime BETWEEN :start AND :end")
    List<Fact_pointage> findByPastilleIdAndEventTimeBetween(
            @Param("pastilleId") Long pastilleId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Recherche par agent et période
    @Query("SELECT fp FROM Fact_pointage fp WHERE fp.agentUserId = :agentUserId AND fp.eventTime BETWEEN :start AND :end")
    List<Fact_pointage> findByAgentUserIdAndEventTimeBetween(
            @Param("agentUserId") Long agentUserId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Recherche par statut et période
    @Query("SELECT fp FROM Fact_pointage fp WHERE fp.processedStatus = :status AND fp.eventTime BETWEEN :start AND :end")
    List<Fact_pointage> findByProcessedStatusAndEventTimeBetween(
            @Param("status") String status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}