package com.teknokote.ess.core.model.movements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
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
public class TankLevelPerSales extends ESSEntity<Long, User>
{

    private Long tank;
    private String fuelGrade;
    private Long pumpTransactionId;
    private double salesVolume;       // Volume vendu ( 10 litres)
    private double tankVolumeChanges; // Volume restant dans le cuve constaté à travers ReportTankMeasurement (Probe)
    private double changedVolume;     // tankVolumeChanges(i-1) - tankVolumeChanges(i)
    private LocalDateTime dateTime;
    @ManyToOne
    @JsonIgnore
    public ControllerPts controllerPts;
    @ManyToOne
    @JsonIgnore
    public ControllerPtsConfiguration controllerPtsConfiguration;

}

/**
 * -------------------------
 * Principe de vérification
 * -------------------------
 *          Tank     SalesVolume     tankVolumeChanges   changedVolume   dateTime
 * Ligne 1   Tank1    10l            1000 l                 -               t1
 * Ligne 2   Tank1    20l            980 l                  20l             t2
 * Ligne 3   Tank1    40l            930 l                  50l             t2 ===> >>>> Fuite vu que SalesVolume est différent de changedVolume
 */
