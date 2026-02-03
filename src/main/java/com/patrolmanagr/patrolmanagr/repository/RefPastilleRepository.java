package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefPastilleRepository extends JpaRepository<Ref_pastille, Long> {

    @Query("select ref_pastille from Ref_pastille ref_pastille where ref_pastille.id = :id")
    Ref_pastille findByIdPastille(@Param("id") Long id);

    @Query("select ref_pastille from Ref_pastille ref_pastille where ref_pastille.ref_site_id.id = :siteId")
    List<Ref_pastille> findByIdSite(@Param("siteId") Long siteId);

    @Query("select ref_pastille from Ref_pastille ref_pastille where ref_pastille.ref_secteur_id.id = :secteurId")
    List<Ref_pastille> findByIdSecteur(@Param("secteurId") Long secteurId);

    @Query("select ref_pastille from Ref_pastille ref_pastille where ref_pastille.code = :code")
    Ref_pastille findByCode(@Param("code") String code);

    @Query("select ref_pastille from Ref_pastille ref_pastille where ref_pastille.label = :label")
    Ref_pastille findByLabel(@Param("label") String label);

    // NOUVELLE MÉTHODE CRITIQUE pour la table fact_pointage
    @Query("select ref_pastille from Ref_pastille ref_pastille where ref_pastille.external_uid = :external_uid")
    Ref_pastille findByExternalUid(@Param("external_uid") String external_uid);

    // Pour le traitement batch des imports
    @Query("select ref_pastille from Ref_pastille ref_pastille where ref_pastille.external_uid IN :external_uids")
    List<Ref_pastille> findByExternalUidIn(@Param("external_uids") List<String> external_uids);

    // Recherche avec cache pour performance
    @Query("select ref_pastille from Ref_pastille ref_pastille where ref_pastille.external_uid = :external_uid AND ref_pastille.status = com.patrolmanagr.patrolmanagr.config.Status.ACTIVE")
    Ref_pastille findActiveByExternalUid(@Param("external_uid") String external_uid);
}