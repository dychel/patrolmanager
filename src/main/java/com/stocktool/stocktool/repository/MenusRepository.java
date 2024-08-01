package com.stocktool.stocktool.repository;

import com.stocktool.stocktool.entity.Menus;
import com.stocktool.stocktool.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@Repository
public interface MenusRepository extends JpaRepository<Menus, Long> {
  //  Menus findByNom_menu(String nom_menu);
    @Query("select menus from Menus menus where menus.id= :id")
    Menus findByIdMenus(@Param("id") Long id);
//    @Query("select menu from Menus menu where menu.produit.id=:id")
//    List<Menus> findMenusByProduit(@PathVariable("id") Long id);
}
