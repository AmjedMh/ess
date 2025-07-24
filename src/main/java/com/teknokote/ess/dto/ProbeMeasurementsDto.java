package com.teknokote.ess.dto;

import lombok.Data;

@Data

public class ProbeMeasurementsDto {
    private Long probe;
    private Long fuelGradeId;
    private String fuelGradeName;
    private String status;
    private float productHeight;
    private float waterHeight;
    private float temperature;
    private Double productVolume;

    private Double waterVolume;

    private Double productUllage;

    private Double productTCVolume;


    private float productDensity;

    private float productMass;

    private Long tankFillingPercentage;

}
