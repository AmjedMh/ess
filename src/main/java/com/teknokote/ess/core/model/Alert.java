package com.teknokote.ess.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Alert extends ESSEntity<Long, User> {

    private String dateTime;
    private String deviceType;
    private Long deviceNumber;
    private String state;
    private Long code;
    private String configurationId;
    @Enumerated(EnumType.STRING)
    private EnumAlertStatus status;
    @ManyToOne
    @JsonIgnore
    public ControllerPts controllerPts;
    private String alertDescription;

    private static final String OFFLINE_STATE_DETECTED = "Offline state detected";
    private static final String ERROR_DETECTED = "error detected";

    public enum DeviceType {
        PTS, PUMP, PROBE, PRICE_BOARD, READER
    }

    public Alert(String dateTime, String deviceType, Long code, Long deviceNumber, String state, String configurationId) {
        this.dateTime = dateTime;
        this.deviceType = deviceType;
        this.code = code;
        this.deviceNumber = deviceNumber;
        this.state = state;
        this.configurationId = configurationId;
        setAlertDescription(DeviceType.valueOf(deviceType), code);
    }

    public void mailSent() {
        this.setStatus(EnumAlertStatus.SENT);
    }

    public void setAlertDescription(DeviceType deviceType, Long code) {
        Map<Long, String> codeDescriptionMap = codeDescriptions.get(deviceType);
        if (codeDescriptionMap == null) {
            throw new IllegalArgumentException("Invalid DeviceType");
        }

        String description = codeDescriptionMap.get(code);
        if (description == null) {
            throw new IllegalArgumentException("Invalid code value for DeviceType " + deviceType);
        }
        this.alertDescription = description;
    }

    private static final Map<DeviceType, Map<Long, String>> codeDescriptions = new EnumMap<>(DeviceType.class);

    static {
        // Initialize descriptions for PTS
        Map<Long, String> ptsDescriptions = new HashMap<>();
        ptsDescriptions.put(1L, "low battery voltage detected");
        ptsDescriptions.put(2L, "high CPU temperature detected");
        ptsDescriptions.put(3L, "power down detected");
        ptsDescriptions.put(4L, "restart detected");
        codeDescriptions.put(DeviceType.PTS, ptsDescriptions);

        // Initialize descriptions for Pump
        Map<Long, String> pumpDescriptions = new HashMap<>();
        pumpDescriptions.put(1L, OFFLINE_STATE_DETECTED);
        pumpDescriptions.put(2L, "Overfilling detected");
        codeDescriptions.put(DeviceType.PUMP, pumpDescriptions);

        // Initialize descriptions for Reader
        Map<Long, String> readerDescriptions = new HashMap<>();
        readerDescriptions.put(1L, OFFLINE_STATE_DETECTED);
        readerDescriptions.put(2L, ERROR_DETECTED);
        codeDescriptions.put(DeviceType.READER, readerDescriptions);

        // Initialize descriptions for PriceBoard
        Map<Long, String> priceBoardDescriptions = new HashMap<>();
        priceBoardDescriptions.put(1L, OFFLINE_STATE_DETECTED);
        priceBoardDescriptions.put(2L, ERROR_DETECTED);
        codeDescriptions.put(DeviceType.PRICE_BOARD, priceBoardDescriptions);

        // Initialize descriptions for Probe (similarly for PriceBoard and Reader)
        Map<Long, String> probeDescriptions = new HashMap<>();
        probeDescriptions.put(1L, OFFLINE_STATE_DETECTED);
        probeDescriptions.put(2L, ERROR_DETECTED);
        probeDescriptions.put(3L, "Critical high product level detected");
        probeDescriptions.put(4L, "High product level detected");
        probeDescriptions.put(5L, "Low product level detected");
        probeDescriptions.put(6L, "Critical low product level detected");
        probeDescriptions.put(7L, "High water level detected");
        probeDescriptions.put(8L, "Tank leakage detected");
        codeDescriptions.put(DeviceType.PROBE, probeDescriptions);

    }

}
