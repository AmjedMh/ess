package com.teknokote.ess.core.model.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.ess.core.model.common.ControllerIdentifiedESSEntity;
import com.teknokote.ess.core.model.movements.PumpTransaction;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FuelGrade extends ControllerIdentifiedESSEntity {
    private static final long serialVersionUID = -4868481503521344224L;

    private String name;
    private Double price;
    private Double expansionCoefficient;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "fuelGrade")
    private transient List<PumpTransaction> pumpTransactions;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    public ControllerPts controllerPts;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    public ControllerPtsConfiguration controllerPtsConfiguration;

}

