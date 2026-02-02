package com.patrolmanagr.patrolmanagr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ref_clientDTO {

    private Long id;
    private String code;
    private String name;
    private String email;
    private String telephone;
    private String adresse;
    private Long status;
    private String audit_field;
}