package com.teknokote.ess.core.model;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Permission extends ESSEntity<Long, User> {

    private static final long serialVersionUID = -8626089542655762138L;


    private Boolean configuration;
    private Boolean control;
    private Boolean monitoring;
    private Boolean reports;

}
