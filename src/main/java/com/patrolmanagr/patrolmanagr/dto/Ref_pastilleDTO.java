package com.patrolmanagr.patrolmanagr.dto;
import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.entity.Ref_secteur;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ref_pastilleDTO {

    private String code;
    private String label;
    private Long refSiteId;
    private Long refSecteurId;
    private String external_uid;
    private Status status;
    private String audit_field;
}
