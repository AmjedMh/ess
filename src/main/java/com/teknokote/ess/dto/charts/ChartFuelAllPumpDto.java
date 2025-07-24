package com.teknokote.ess.dto.charts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class ChartFuelAllPumpDto {
    private String nameF;
    private String dateF;
    private Double sumF;

    public ChartFuelAllPumpDto(String nameF,String dateF, Double sumF) {
        this.nameF = nameF;
        this.dateF = dateF;
        this.sumF = sumF;
    }
}
