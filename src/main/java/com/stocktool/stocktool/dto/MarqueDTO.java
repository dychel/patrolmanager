package com.stocktool.stocktool.dto;

import com.stocktool.stocktool.entity.Marque;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class MarqueDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String marque;
    private String note;

    public MarqueDTO() {
    }

    public MarqueDTO(Marque marque) {
        this.marque = marque.getMarque();
        this.note = marque.getNote();
    }

    public Marque toMarque(){
        return new Marque(marque, note);
    }
}
