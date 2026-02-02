package com.patrolmanagr.patrolmanagr.entity;

import com.patrolmanagr.patrolmanagr.config.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ref_ronde")
public class Ref_ronde {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;

    @ManyToOne
    @JoinColumn(name = "ref_site_id")
    private Ref_site ref_site;

    @Column(name = "heure_debut")
    private LocalTime heure_debut;

    @Column(name = "heure_fin")
    private LocalTime heure_fin;

    @Column(name = "duree_theorique")
    private Integer duree_theorique = 0;

    private Integer expected_duration_min = 0;
    private Integer delay_tolerance_sec = 0;

    // CHANGEMENT ICI : String au lieu de Status
    @Column(name = "status", length = 20)
    private Status status;

    private String audit_field;

    // NOUVEAU CHAMP
    @Column(name = "date_ronde")
    private LocalDate date;

    // NOUVEAU CHAMP : Jours de la semaine
    @Column(name = "jours_semaine", length = 50)
    private String joursSemaine; // Format: "L,Ma,Me,J,V,S,D"

    // Champs historique activités
    private LocalDateTime created_at = LocalDateTime.now();
    private Long created_by;
    private LocalDateTime updated_at;
    private Long updated_by;
    private Boolean is_deleted = false;
    private LocalDateTime deleted_at;
    private Long deleted_by;
}