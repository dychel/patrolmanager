package com.stocktool.stocktool.repository;

import com.stocktool.stocktool.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    @Query("select categorie from Categorie categorie where categorie.id = :id")
    Categorie findByIdCategorie(@Param("id") Long id);
}
