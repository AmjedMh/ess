package com.teknokote.ess.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class TankConfigDto {

    private Long idConf;
    private Long fuelGradeId;
    private String fuelGrade;
    private Long height;
    private Long criticalHighProductAlarm;
    private Long highProductAlarm;
    private Long lowProductAlarm;
    private Long criticalLowProductAlarm;
    private Long highWaterAlarm;
    private Boolean stopPumpsAtReachingCriticalLowProductHeight;
}
