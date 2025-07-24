package com.teknokote.ess.core.model.movements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.configuration.Nozzle;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PumpTransaction extends ESSEntity<Long, User> {
    /**
     * Référence de la transaction niveau contrôleur de la station
     */
    private Long transactionReference;
    private String configurationId;
    private LocalDateTime dateTimeStart;
    private LocalDateTime dateTime;
    private Double volume;
    private float tcvolume;
    private float price;
    private Double amount;
    private BigDecimal totalVolume;
    private BigDecimal totalAmount;
    private String tag;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private EnumTransaction type;
    @Enumerated(EnumType.STRING)
    private EnumTransactionState state;
    @ManyToOne
    @JsonIgnore
    public Pump pump;
    @ManyToOne
    @JsonIgnore
    public Nozzle nozzle;
    @ManyToOne
    @JsonIgnore
    public FuelGrade fuelGrade;
    @ManyToOne
    @JsonIgnore
    public PumpAttendant pumpAttendant;
    @Column(name = "pump_attendant_id", insertable = false, updatable = false)
    private Long pumpAttendantId;
    @ManyToOne
    @JsonIgnore
    public ControllerPtsConfiguration controllerPtsConfiguration;
    @ManyToOne
    @JsonIgnore
    public ControllerPts controllerPts;
}
