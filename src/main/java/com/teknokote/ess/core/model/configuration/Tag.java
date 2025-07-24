package com.teknokote.ess.core.model.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Entity;
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
public class Tag extends ESSEntity<Long, User>
{

    private String tags;
    private String name;
    private Boolean valid;
    private Boolean present;
    @ManyToOne
    @JsonIgnore
    public Pump pump;
    @ManyToOne
    @JsonIgnore
    public Nozzle nozzle;
}
