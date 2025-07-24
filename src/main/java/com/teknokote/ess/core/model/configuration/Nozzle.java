package com.teknokote.ess.core.model.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.ess.core.model.common.ControllerIdentifiedESSEntity;
import com.teknokote.ess.core.model.movements.PumpTransaction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Nozzle extends ControllerIdentifiedESSEntity
{

    private String  description;
    @ManyToOne
    @JsonIgnore
    public Pump pump;
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "nozzle")
    private List<PumpTransaction> pumpTransactions;
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "nozzle",cascade = CascadeType.ALL)
    private List<Tag> tags;
    @ManyToOne
    @JsonIgnore
    public Tank tank;
    @ManyToOne
    @JsonIgnore
    public FuelGrade grade;
    @ManyToOne
    @JsonIgnore
    public ControllerPts controllerPts;
    @ManyToOne
    @JsonIgnore
    public ControllerPtsConfiguration controllerPtsConfiguration;

}
