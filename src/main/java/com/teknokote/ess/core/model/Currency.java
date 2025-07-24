package com.teknokote.ess.core.model;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Currency extends ESSEntity<Long, User> {

    @Serial
    private static final long serialVersionUID = 1361988953789849032L;
    private String name;
    private String code;
    private String locale;
}
