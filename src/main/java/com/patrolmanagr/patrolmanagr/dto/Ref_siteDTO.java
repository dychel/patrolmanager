package com.patrolmanagr.patrolmanagr.dto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ref_siteDTO {

    private Long id;
    private String code;
    private String name;
    private Long zoneId;
    private String client_name;
    private Long status;
    private String audit_field;
}
