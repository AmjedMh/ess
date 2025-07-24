package com.teknokote.ess.dto.charts;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Data
@Getter
@Setter
public class TankLevelPerSalesChartDto {
    private Long tank;
    private double productVolume;
    private LocalDateTime dateTime;

    public TankLevelPerSalesChartDto(Long tank, double productVolume, LocalDateTime dateTime) {
        this.tank = tank;
        this.productVolume = productVolume;
        this.dateTime = dateTime;
    }
}
