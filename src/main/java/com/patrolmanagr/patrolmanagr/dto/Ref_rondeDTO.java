package com.patrolmanagr.patrolmanagr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ref_rondeDTO {

    private Long id;
    private String code;
    private String name;
    private Long siteId;
    private Integer expected_duration_min;
    private Integer delay_tolerance_sec;

    // NOUVEAU : Jours de la semaine
    private String joursSemaine; // Format: "L,Ma,Me,J,V,S,D"

    private String date;

    // NOUVEAUX CHAMPS AJOUTÉS
    private LocalTime heure_debut;
    private LocalTime heure_fin;
    private Integer duree_theorique;

    private Long status;
    private String audit_field;
}