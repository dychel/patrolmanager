package com.patrolmanagr.patrolmanagr.entity;
import com.patrolmanagr.patrolmanagr.config.Mode;
import com.patrolmanagr.patrolmanagr.config.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Ref_vendor_api {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String vendor_code;
    private Mode mode;
    private String base_url;
    private Status status;
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
