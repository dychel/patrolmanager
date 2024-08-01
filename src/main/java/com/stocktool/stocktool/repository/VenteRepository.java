package com.stocktool.stocktool.repository;

import com.stocktool.stocktool.entity.Ravitaillement;
import com.stocktool.stocktool.entity.Vente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Repository
public interface VenteRepository extends JpaRepository<Vente, Long> {

    @Query("select vente from Vente vente where vente.id= :id")
    Vente findByIdVente(@Param("id") Long id);

    @Query("select vente from Vente vente where vente.menus.id=:id")
    List<Vente> findVenteByMenu(@PathVariable("id") Long id);
    @Query("select vente from Vente vente where vente.user.id=:id")
    List<Vente> findVenteByUser(@PathVariable("id") Long id);

    @Query("select vente from Vente vente where vente.equipe.id=:id")
    List<Vente> findVenteByEquipe(@PathVariable("id") Long id);
}
