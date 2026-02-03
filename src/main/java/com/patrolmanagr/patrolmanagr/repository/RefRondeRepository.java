package com.patrolmanagr.patrolmanagr.repository;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
import com.patrolmanagr.patrolmanagr.entity.Ref_secteur;
import com.patrolmanagr.patrolmanagr.entity.Ref_terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefRondeRepository extends JpaRepository<Ref_ronde, Long>  {

    @Query("select ref_ronde from Ref_ronde ref_ronde where ref_ronde.id = :id")
    Ref_ronde findByIdRonde(@Param("id") Long id);

//    @Query("select ref_ronde from Ref_ronde ref_ronde where ref_ronde.ref_site.id = :siteId")
//    List<Ref_ronde> findByIdSite(@Param("siteId") Long siteId);
// Dans RefRondeRepository, assurez-vous d'avoir :
    @Query("select ref_ronde from Ref_ronde ref_ronde where ref_ronde.ref_site.id = :siteId")
    List<Ref_ronde> findByIdSite(@Param("siteId") Long siteId);

    @Query("select ref_ronde from Ref_ronde ref_ronde where ref_ronde.ref_site.id = :siteId and ref_ronde.status = com.patrolmanagr.patrolmanagr.config.Status.ACTIVE")
    List<Ref_ronde> findActiveBySiteId(@Param("siteId") Long siteId);

    @Query("select ref_ronde from Ref_ronde ref_ronde where ref_ronde.code = :code")
    Ref_ronde findByCode(@Param("code") String code);

//    @Query("select ref_ronde from Ref_ronde ref_ronde where ref_ronde.name = :name")
//    Ref_ronde findByName(@Param("name") String name);
}
