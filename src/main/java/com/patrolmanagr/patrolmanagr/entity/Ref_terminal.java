package com.patrolmanagr.patrolmanagr.entity;
import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.config.Terminal_Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Ref_terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Terminal_Type terminalType;
    private String code;
    private String serial_number;
    private String vendor_external_id;
    @ManyToOne
    @JoinColumn(name = "ref_vendor_api_id")
    private Ref_vendor_api ref_vendor_id;
    @ManyToOne
    @JoinColumn()
    private Ref_site ref_site;
    private Status status;
    private Date last_seen_at;
    private String audit_field;

    //champs historique activtés
    private LocalDateTime created_at = LocalDateTime.now();
    private Long created_by;
    private LocalDateTime updated_at;
    private Long updated_by;
    private Boolean is_deleted = false;
    private LocalDateTime deleted_at;
    private Long deleted_by;
}
