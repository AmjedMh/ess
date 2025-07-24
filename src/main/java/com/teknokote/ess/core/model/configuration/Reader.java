package com.teknokote.ess.core.model.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.ess.core.model.common.ControllerIdentifiedESSEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reader extends ControllerIdentifiedESSEntity
{
    private static final long serialVersionUID = -2067041299382215769L;

    private Long address;
    private Long pumpId;
    private Long readers;
    private String tag;
    private Boolean online;
    private Boolean error;
    @ManyToOne
    @JsonIgnore
    public ControllerPtsConfiguration controllerPtsConfiguration;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "port_id", referencedColumnName = "id")
    public ProbePort port;
}
