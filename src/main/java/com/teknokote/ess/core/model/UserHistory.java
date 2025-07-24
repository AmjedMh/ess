package com.teknokote.ess.core.model;

import com.teknokote.core.model.SimpleESSEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serial;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserHistory extends SimpleESSEntity<Long>
{
    @Serial
    private static final long serialVersionUID = 5736500306846413972L;

    /**
     * Id du user qui a procédé à l'action
     */
    private Long userId;
    /**
     * Type de l'activité: POST, GET, DELETE, PUT,...
     */
    @Enumerated(EnumType.STRING)
    private EnumActivityType activityType;
    /**
     * URI
     */
    private String requestURI;
    /**
     * Request handler method
     */
    private String requestHandler;
    /**
     * Adresse IP du client
     */
    private String ipAddress;
    private LocalDateTime activityDate;
    /**
     * Id du contrôleur si l'action implique un contrôleur
     */
    private Long controllerPtsId;
    private boolean impersonationMode;
    private String impersonatorUserName;
    private String action;
    private String userName;
}
