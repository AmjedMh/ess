package com.teknokote.ess.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
public class TankMeasurementsDto {
    private LocalDateTime dateTime;
    private Long tank;
    private String fullGrade;
    private String status;
    private String alarms;
    private Double productHeight;
    private Double waterHeight;
    private Double temperature;
    private Double productVolume;
    private Double waterVolume;
    private Double productUllage;
    private Double productTCVolume;
    private Double productDensity;
    private Double productMass;
    private Double tankFillingPercentage;

}
