package com.patrolmanagr.patrolmanagr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketPointageDTO {
    private String externalUid;      // "56E7C660"
    private String terminalCode;     // "RFID-001"
    private String agentCode;        // "AGENT-101"
    private String siteCode;         // "1"
    private String timestamp;        // "2024-02-03T16:30:00"
    private String rawData;          // JSON brut

    // Constructeur minimal
    public WebSocketPointageDTO(String externalUid, String terminalCode) {
        this.externalUid = externalUid;
        this.terminalCode = terminalCode;
        this.timestamp = LocalDateTime.now().toString();
    }
}