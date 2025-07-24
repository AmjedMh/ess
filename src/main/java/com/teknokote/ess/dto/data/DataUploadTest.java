package com.teknokote.ess.dto.data;

import lombok.Data;

import java.util.List;

@Data
public class DataUploadTest {
    private String configurationId;
    private List<Long> pumpsOfflineStatus;
    private List<Long> pumpsErrorDetected;
    private List<Long> probesOfflineStatus;
    private List<Long> probesErrorDetected;
    private List<Long> priceBoardsOfflineStatus;
    private List<Long> priceBoardsErrorDetected;
    private List<Long> readersOfflineStatus;
    private List<Long> readersErrorDetected;
    private List<Long> tanksCriticalHighProductAlarm;
    private List<Long> tanksHighProductAlarm;
    private List<Long> tanksLowProductAlarm;
    private List<Long> tanksCriticalLowProductAlarm;
    private List<Long> tanksHighWaterAlarm;
    private List<Long> tanksLeakageDetected;
    private List<Long> tanksProbeStuckDetected;
    private Long ptsStartupSeconds;
    private Boolean ptsBatteryLowVoltageDetected;
    private Boolean ptsPowerDownDetected;
}
