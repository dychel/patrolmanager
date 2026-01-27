package com.patrolmanagr.patrolmanagr.dto;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ref_zoneDTO {

    private String code;
    private String name;
    private Long status;
    private String audit_field;

    //champs historique activtés
    private LocalDateTime created_at = LocalDateTime.now();
    private LocalDateTime created_by;
    private LocalDateTime updated_at;
    private LocalDateTime updated_by;
    private Boolean is_deleted = false;
    private LocalDateTime deleted_at;
    private Long deleted_by;

}
