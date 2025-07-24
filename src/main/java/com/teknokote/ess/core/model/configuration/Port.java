package com.teknokote.ess.core.model.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Port extends ESSEntity<Long, User>
{

    private Long protocol;
    private Long baudRate;
    @Enumerated(EnumType.STRING)
    @Column(insertable=false, updatable=false)
    private EnumPortType type;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    public ControllerPtsConfiguration controllerPtsConfiguration;
}
