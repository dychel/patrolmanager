package com.patrolmanagr.patrolmanagr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RondePastilleOrderDTO {
    private Long rondeId;
    private List<PastilleSequenceDTO> pastilles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PastilleSequenceDTO {
        private Long pastilleId;
        private String externalUid;
        private Integer seq_no;
        private String label;
        private String secteur;
        private String technologie;
    }
}