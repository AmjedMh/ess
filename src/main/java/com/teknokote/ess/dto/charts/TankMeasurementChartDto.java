package com.teknokote.ess.dto.charts;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class TankMeasurementChartDto {
    private LocalDateTime dateTime;
    private Long tank;
    private double productVolume;

    public TankMeasurementChartDto(LocalDateTime dateTime, Long tank, double productVolume) {
        this.dateTime = dateTime;
        this.tank = tank;
        this.productVolume = productVolume;
    }
}
