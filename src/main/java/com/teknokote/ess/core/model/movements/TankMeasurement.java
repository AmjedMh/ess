package com.teknokote.ess.core.model.movements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "report_tank_measurement")
public class TankMeasurement extends ESSEntity<Long, User>
{

    private String configurationId;
    private LocalDateTime dateTime;
    private Long tank;
    private String fullGrade;
    private Long fuelGradeId;
    private String status;
    private String alarms;
    private Double productHeight;
    private Double waterHeight;
    private Double temperature;
    private Double productVolume;
    private Double waterVolume;
    private Double productUllage;
    private Double productTCVolume;
    private Double productDensity;
    private Double productMass;
    @ManyToOne
    @JsonIgnore
    public ControllerPts controllerPts;
    @ManyToOne
    @JsonIgnore
    public ControllerPtsConfiguration controllerPtsConfiguration;
}
