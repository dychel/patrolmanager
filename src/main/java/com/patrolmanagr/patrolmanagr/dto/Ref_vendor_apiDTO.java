package com.patrolmanagr.patrolmanagr.dto;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ref_vendor_apiDTO {

    private String name;
    private String vendor_code;
    private Long mode;
    private String base_url;
    private Long status;
    private String audit_field;
}
