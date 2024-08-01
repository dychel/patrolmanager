package com.stocktool.stocktool.service;
import com.stocktool.stocktool.dto.MenusDTO;
import com.stocktool.stocktool.entity.Menus;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface MenusService {
    Menus saveMenus(MenusDTO menusDTO);
    Menus updateMenus(Long id, MenusDTO produitDTO);
    Menus findMenusById(Long id);
    ResponseEntity<ResponseMessage> listMenus();
    void deleteMenusById(Long id);
//    List<Menus> getMenusByProduit(Long id);
}
