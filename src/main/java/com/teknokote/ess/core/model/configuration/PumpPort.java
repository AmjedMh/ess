package com.teknokote.ess.core.model.configuration;


import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue(value = EnumPortType.PortType.PUMP)
public class PumpPort extends Port {
    private static final long serialVersionUID = -358330309792589700L;
    @Column(name = "pump_port_id_configured")
    private Long idConfigured;
}
