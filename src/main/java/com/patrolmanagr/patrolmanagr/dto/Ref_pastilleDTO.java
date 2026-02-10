package com.patrolmanagr.patrolmanagr.dto;

import com.patrolmanagr.patrolmanagr.config.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ref_pastilleDTO {
    private Long id;
    private String code;
    private String label;
    private Long refSiteId;
    private Long refSecteurId;
    private String external_uid;
    private Integer tempsTheorique; // Temps théorique en secondes
    private Status status;
    private String audit_field;
}