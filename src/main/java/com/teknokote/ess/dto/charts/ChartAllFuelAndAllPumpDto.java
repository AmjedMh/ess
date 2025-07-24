package com.teknokote.ess.dto.charts;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
public class ChartAllFuelAndAllPumpDto {
    private String date;
    private Double sum;

    public ChartAllFuelAndAllPumpDto(String date, Double sum) {
        this.date = date;
        this.sum = sum;
    }
}
