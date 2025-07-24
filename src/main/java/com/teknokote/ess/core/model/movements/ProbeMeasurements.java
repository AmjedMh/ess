package com.teknokote.ess.core.model.movements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class ProbeMeasurements  extends ESSEntity<Long, User>{
    private static final long serialVersionUID = -1396950709135651246L;

    private Long probe;
    private Long fuelGradeId;
    private String fuelGradeName;
    private String status;
    private float productHeight;
    private float waterHeight;
    private float temperature;
    private Double productVolume;
    private Double waterVolume;
    private Double productUllage;
    private Double productTCVolume;
    private float productDensity;
    private float productMass;
    private Long tankFillingPercentage;
    @ManyToOne
    @JsonIgnore
    public ControllerPtsConfiguration controllerPtsConfiguration;
}
