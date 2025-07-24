package com.teknokote.ess.core.model.common;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class ControllerIdentifiedESSEntity extends ESSEntity<Long, User>
{
    /**
     * Champs dédié à récupérer l'identifiant technique de l'élément à partir de la configuration du contrôleur
     */
   // @Column(nullable = false)
    protected Long idConf;
}
