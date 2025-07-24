package com.teknokote.ess.core.model.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.ess.core.model.common.ControllerIdentifiedESSEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Probe  extends ControllerIdentifiedESSEntity {
    private static final long serialVersionUID = -8941577210506841163L;

    private Long address;
    @ManyToOne
    @JsonIgnore
    public ProbePort port;
    @OneToOne(cascade = CascadeType.ALL)
    private Tank tank;
    @ManyToOne
    @JsonIgnore
    public ControllerPtsConfiguration controllerPtsConfiguration;
}
