package com.teknokote.ess.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PumpPrice extends ESSEntity<Long, User>
{

    @Column(name = "price")
    private double[] price;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "pump_id")
    public Pump pump;

    @Column(name = "users")
    private String user;

}
