package com.patrolmanagr.patrolmanagr.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ref_ronde_pastille {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "ref_ronde_id")
    private Ref_ronde ref_ronde_id;
    @ManyToOne
    @JoinColumn(name = "ref_pastille_id")
    private Ref_pastille ref_pastille_id;
    @NotNull
    private Integer seq_no;
    private Integer expected_travel_sec;
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
