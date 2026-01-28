package com.patrolmanagr.patrolmanagr.entity;
import com.patrolmanagr.patrolmanagr.config.Scheduled_Type;
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
public class Prog_ronde {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "ref_ronde_id")
    private Ref_ronde ref_ronde;
    @ManyToOne
    @JoinColumn(name = "ref_site_id")
    private Ref_site ref_site;
    private Scheduled_Type scheduledType;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User assigned_agent_id;
    @ManyToOne
    @JoinColumn(name = "ref_terminal_id")
    private Ref_terminal assigned_rondier_terminal_id;
    private Status status;
    private String audit_field;
    private String schedule_payload;

    //champs historique activtés
    private LocalDateTime created_at = LocalDateTime.now();
    private Long created_by;
    private LocalDateTime updated_at;
    private Long updated_by;
    private Boolean is_deleted = false;
    private LocalDateTime deleted_at;
    private Long deleted_by;

}
