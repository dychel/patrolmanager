package com.stocktool.stocktool.repository;
import com.stocktool.stocktool.entity.Composition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@Repository
public interface CompositionRepository extends JpaRepository<Composition, Long> {
    @Query("select composition from Composition composition where composition.id= :id")
    Composition findByIdComposition(@Param("id") Long id);
    @Query("select compo from Composition compo where compo.produit.id=:id")
    List<Composition> findCompositionByProduit(@PathVariable("id") Long id);
    @Query("select compo from Composition compo where compo.menus.id=:id")
    List<Composition> findCompositionByMenu(@PathVariable("id") Long id);
}
