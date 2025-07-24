package com.teknokote.ess.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class ProbeTankVolumeForHeight extends ESSEntity<Long, User>
{

    private Long probe;
    private float height;
    private float volume;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "idCont")
    public ControllerPts controllerPts;
}
