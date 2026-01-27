package com.patrolmanagr.patrolmanagr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefRondePastilleDTO {

    private Long refRondeId;
    private Long refPastilleId;
    private Integer seq_no;
    private Integer expected_travel_sec;
    private String audit_field;
}
