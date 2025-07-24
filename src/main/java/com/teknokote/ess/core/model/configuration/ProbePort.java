package com.teknokote.ess.core.model.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue(value = EnumPortType.PortType.PROBE)
public class ProbePort extends Port {
    private static final long serialVersionUID = 1205370316920052389L;

    @Column(name = "probe_port_id_configured")
    private String idConfigured;
}
