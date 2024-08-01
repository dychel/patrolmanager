package com.stocktool.stocktool.repository;

import com.stocktool.stocktool.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    Produit findByLibelle(String Libelle);
    @Query("select produit from Produit produit where produit.id= :id")
    Produit findByIdProduit(@Param("id") Long id);
    @Query("select prod from Produit prod where prod.unite.id=:id")
    List<Produit> findProduitByUnite(@PathVariable("id") Long id);
    @Query("select prod from Produit prod where prod.marque.id=:id")
    List<Produit> findProduitByMarque(@PathVariable("id") Long id);
    @Query("select prod from Produit prod where prod.categorie.id=:id")
    List<Produit> findProduitByCategorie(@PathVariable("id") Long id);
}
