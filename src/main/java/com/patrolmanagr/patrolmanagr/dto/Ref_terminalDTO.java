package com.patrolmanagr.patrolmanagr.dto;
import com.patrolmanagr.patrolmanagr.config.Terminal_Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ref_terminalDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Terminal_Type terminalType;
    private String code;
    private String serial_number;
    private String vendor_external_id;
    //private ref_vendor_api vendor_id;
    private Long siteId;
    private Long vendorId;
    private Long status;
    private Date last_seen_at;
    private String audit_field;

}
