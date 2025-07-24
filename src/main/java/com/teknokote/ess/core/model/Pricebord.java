package com.teknokote.ess.core.model;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Pricebord extends ESSEntity<Long, User>
{

    private Long price;
    private Boolean online;
    private Boolean error;
}
