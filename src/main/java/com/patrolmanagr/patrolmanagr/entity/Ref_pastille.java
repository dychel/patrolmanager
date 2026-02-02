package com.patrolmanagr.patrolmanagr.entity;
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
public class Ref_pastille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String label;

    @ManyToOne
    @JoinColumn(name = "ref_site_id")
    private Ref_site ref_site_id;

    @ManyToOne
    @JoinColumn(name = "ref_secteur_id")
    private Ref_secteur ref_secteur_id;

    private String external_uid;

    @Column(name = "temps_theorique")
    private Integer tempsTheorique; // Temps théorique en secondes

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