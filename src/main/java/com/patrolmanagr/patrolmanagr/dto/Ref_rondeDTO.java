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
public class Ref_rondeDTO {

    private Long id;
    private String code;
    private String name;
    private Long siteId;
    private int expected_duration_min;
    private int delay_tolerance_sec;
    private Long status;
    private String audit_field;
}
