package com.patrolmanagr.patrolmanagr.repository;
import com.patrolmanagr.patrolmanagr.entity.Ref_secteur;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

@Repository
public interface RefSecteurRepository extends JpaRepository<Ref_secteur, Long> {
    @Query("select ref_secteur from Ref_secteur ref_secteur where ref_secteur.id = :id")
    Ref_secteur findByIdSecteur(@Param("id") Long id);
    @Query("select ref_secteur from Ref_secteur ref_secteur where ref_secteur.name = :name")
    Ref_secteur findByName(@Param("name") String name);
    @Query("select ref_secteur from Ref_secteur ref_secteur where ref_secteur.code = :code")
    Ref_secteur findByCode(@Param("code") String code);
    @Query("select ref_secteur from Ref_secteur ref_secteur where ref_secteur.ref_site.id = :id")
    Ref_secteur findSecteurBySite(@PathVariable("id") Long id);
}
