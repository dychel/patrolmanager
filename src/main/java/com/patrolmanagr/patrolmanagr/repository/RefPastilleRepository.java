package com.patrolmanagr.patrolmanagr.repository;
import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
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

    @Query("select ref_pastille from Ref_pastille ref_pastille where ref_pastille.ref_secteur_id.id = :id")
    Ref_pastille findByIdSecteur(@Param("id") Long id);

    @Query("select ref_pastille from Ref_pastille ref_pastille where ref_pastille.code = :code")
    Ref_pastille findByCode(@Param("code") String code);

    @Query("select ref_pastille from Ref_pastille ref_pastille where ref_pastille.label = :label")
    Ref_pastille findByName(@Param("label") String label);
}
