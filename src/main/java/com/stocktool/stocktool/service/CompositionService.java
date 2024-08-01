package com.stocktool.stocktool.service;
import com.stocktool.stocktool.dto.CompositionDTO;
import com.stocktool.stocktool.entity.Composition;
import com.stocktool.stocktool.response.ResponseMessage;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface CompositionService {

    Composition saveComposition(CompositionDTO compositionDTO);
    Composition updateComposition(Long id, CompositionDTO compositionDTO);
    Composition findCompositionById(Long id);
    ResponseEntity<ResponseMessage> listCompositions();
    void deleteCompositionById(Long id);
    List<Composition> getCompositionByProduit(Long id);
    List<Composition> getCompositionByMenu(Long id);
}
