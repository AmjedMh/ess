package com.teknokote.ess.core.model;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "status")
public class StatusModel extends ESSEntity<Long, User> {

    private String configurationId;
    private String dateTime;
    private String firmwareDateTime;
    private Long ptsStartupSeconds;
    private Long startupSeconds;
    private Long batteryVoltage;
    private Long cpuTemperature;
    private Boolean powerDownDetected;
    private Boolean sdMounted;
    private Long pumps;
    private Long probes;
    private Long priceBoards;
    private Long readers;
    private Long gps;

}
