package com.teknokote.ess.dto.charts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChartAllPumpAndFuelDto {
    private String date;
    private double sum;
    private String fuel;

    public ChartAllPumpAndFuelDto(String date, double sum, String fuel) {
        this.date = date;
        this.sum = sum;
        this.fuel = fuel;
    }
}
