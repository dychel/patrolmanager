package com.patrolmanagr.patrolmanagr.dto;

import com.patrolmanagr.patrolmanagr.config.ScheduleTypeJob;
import com.patrolmanagr.patrolmanagr.config.JobScope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysJobDTO {

    private Long id;
    private String jobCode;
    private String name;
    private String description;
    private ScheduleTypeJob scheduleTypeJob;
    private LocalTime scheduleTime;
    private Integer scheduleIntervalMin;
    private JobScope jobScope;
    private String paramsJson;
    private List<Long> rondeIds;
    private Boolean isEnabled = true;
    private String auditField;
}