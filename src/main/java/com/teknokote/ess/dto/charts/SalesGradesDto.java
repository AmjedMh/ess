package com.teknokote.ess.dto.charts;

import lombok.*;

@Data
@Getter
@Setter
public class SalesGradesDto {

    private String fuelGrade;
    private Double totalSalesParAmount;
    private Double totalSalesParVolume;

    public SalesGradesDto(String fuelGrade, Double totalSalesParAmount, Double totalSalesParVolume) {
        this.fuelGrade = fuelGrade;
        this.totalSalesParAmount = totalSalesParAmount;
        this.totalSalesParVolume = totalSalesParVolume;
    }

    public SalesGradesDto() {

    }
}
