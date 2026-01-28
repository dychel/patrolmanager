package com.patrolmanagr.patrolmanagr.dto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ref_secteurDTO {

    private Long id;
    private String code;
    private String name;
    private Long siteId;
    private String client_name;
    private Long status;
    private String audit_field;
}
