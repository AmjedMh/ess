package com.teknokote.ess.dto.charts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class ChartFuelPumpDto {
    private String nameF;
    private Long pump;
    private String dateF;
    private Double sumF;

    public ChartFuelPumpDto(String nameF, Long pump, String dateF, Double sumF) {
        this.nameF = nameF;
        this.pump = pump;
        this.dateF = dateF;
        this.sumF = sumF;
    }
}
