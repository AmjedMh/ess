package com.teknokote.ess.dto.charts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChartAllFuelAndPumpIdDto {
    private Long pump;
    private String date;
    private Double sum;

    public ChartAllFuelAndPumpIdDto(Long pump, String date, Double sum) {
        this.pump = pump;
        this.date = date;
        this.sum = sum;
    }
}
