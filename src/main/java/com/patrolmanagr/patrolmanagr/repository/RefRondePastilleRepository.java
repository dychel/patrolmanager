package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.Ref_ronde_pastille;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefRondePastilleRepository extends JpaRepository<Ref_ronde_pastille, Long> {

    // Trouver toutes les pastilles d'une ronde spécifique
    @Query("SELECT rp FROM Ref_ronde_pastille rp WHERE rp.ref_ronde_id.id = :rondeId")
    List<Ref_ronde_pastille> findByRondeId(@Param("rondeId") Long rondeId);

    // Trouver par ID de pastille
    @Query("SELECT rp FROM Ref_ronde_pastille rp WHERE rp.ref_pastille_id.id = :pastilleId")
    List<Ref_ronde_pastille> findByPastilleId(@Param("pastilleId") Long pastilleId);

    // Trouver par séquence
    @Query("SELECT rp FROM Ref_ronde_pastille rp WHERE rp.seq_no = :seq")
    List<Ref_ronde_pastille> findBySequence(@Param("seq") Integer seq);

    // Trouver par ronde et séquence (pour ordre spécifique)
    @Query("SELECT rp FROM Ref_ronde_pastille rp WHERE rp.ref_ronde_id.id = :rondeId AND rp.seq_no = :seq")
    Ref_ronde_pastille findByRondeIdAndSequence(@Param("rondeId") Long rondeId, @Param("seq") Integer seq);

    // Trouver toutes les pastilles d'une ronde triées par séquence
    @Query("SELECT rp FROM Ref_ronde_pastille rp WHERE rp.ref_ronde_id.id = :rondeId ORDER BY rp.seq_no ASC")
    List<Ref_ronde_pastille> findByRondeIdOrderBySequence(@Param("rondeId") Long rondeId);

    // Vérifier si une pastille existe déjà dans une ronde
    @Query("SELECT COUNT(rp) > 0 FROM Ref_ronde_pastille rp WHERE rp.ref_ronde_id.id = :rondeId AND rp.ref_pastille_id.id = :pastilleId")
    boolean existsByRondeIdAndPastilleId(@Param("rondeId") Long rondeId, @Param("pastilleId") Long pastilleId);

    // Trouver par temps de trajet attendu (supérieur à une valeur)
    @Query("SELECT rp FROM Ref_ronde_pastille rp WHERE rp.expected_travel_sec > :minSeconds")
    List<Ref_ronde_pastille> findByMinTravelTime(@Param("minSeconds") Integer minSeconds);

    // Supprimer toutes les pastilles d'une ronde
    @Query("DELETE FROM Ref_ronde_pastille rp WHERE rp.ref_ronde_id.id = :rondeId")
    void deleteByRondeId(@Param("rondeId") Long rondeId);
}