package com.stocktool.stocktool.repository;

import com.stocktool.stocktool.entity.Categorie;
import com.stocktool.stocktool.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long>{

    @Query("select fournisseur from Fournisseur fournisseur where fournisseur.id = :id")
    Fournisseur findByIdFournisseur(@Param("id") Long id);
}