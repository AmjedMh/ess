package com.teknokote.ess.core.model.configuration;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Une controllerPtsConfiguration est unique pour une configurationId et un contrôleur donné.
 * {@field lastConfigurationUpdate} présente la date de mise en place de la configuration identifiée par
 * {@field configurationId} sur le contrôleur {@field controllerPts}
 */
@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ptsId", "configurationId"}))
public class ControllerPtsConfiguration extends ESSEntity<Long, User> {
    @Serial
    private static final long serialVersionUID = 4416471948371019479L;
    @Column(nullable = false)
    private String configurationId;
    /**
     * identifiant d'un contrôleur. Intégré exprès pour une dénormalisation
     */
    @Column(nullable = false)
    private String ptsId;
    @ManyToOne
    private ControllerPts controllerPts;
    /**
     * Date de mise à jour de la configuration
     */
    private LocalDateTime lastConfigurationUpdate;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "controllerPtsConfiguration", cascade = CascadeType.ALL)
    private List<Pump> pumps;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "controllerPtsConfiguration", cascade = CascadeType.ALL)
    private List<Probe> probes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ControllerPtsConfiguration that = (ControllerPtsConfiguration) o;

        if (!Objects.equals(controllerPts, that.controllerPts)) return false;
        return Objects.equals(this.getConfigurationId(), that.getConfigurationId());
    }

    @Override
    public int hashCode() {
        int result = controllerPts != null ? controllerPts.hashCode() : 0;
        result = 31 * result + (this.getConfigurationId() != null ? this.getConfigurationId().hashCode() : 0);
        return result;
    }

    /**
     * Créer une nouvelle configuration
     */
    public static ControllerPtsConfiguration initialize(ControllerPts controllerPts, String configurationId) {
        return ControllerPtsConfiguration.builder()
                .ptsId(controllerPts.getPtsId())
                .configurationId(configurationId)
                .controllerPts(controllerPts)
                .lastConfigurationUpdate(LocalDateTime.now())
                .pumps(new ArrayList<>())
                .probes(new ArrayList<>())
                .build();
    }
}
