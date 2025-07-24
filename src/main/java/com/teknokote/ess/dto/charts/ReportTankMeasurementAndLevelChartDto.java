package com.teknokote.ess.dto.charts;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Getter
@Setter
public class ReportTankMeasurementAndLevelChartDto
{
    private LocalDateTime dateTime;
    private Long tank;
    private Double measurementVolume;
    private Double levelVolume;

    public ReportTankMeasurementAndLevelChartDto(LocalDateTime dateTime, Long tank, Double measurementVolume, Double levelVolume) {
        this.dateTime = dateTime;
        this.tank = tank;
        this.measurementVolume = measurementVolume;
        this.levelVolume = levelVolume;
    }

    public ReportTankMeasurementAndLevelChartDto() {
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String getDateTime() {
        return dateTime != null ? dateTime.format(formatter) : null;
    }
}
