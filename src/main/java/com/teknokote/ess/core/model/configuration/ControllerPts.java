package com.teknokote.ess.core.model.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.ess.core.model.FirmwareInformation;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.EnumControllerType;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

/**
 * Entité représentant les contrôleurs.
 * Un contrôleur est identifié d'une façon unique selon le {{@code @field} ptsId}
 * Normalement, c'est la seule entité de configuration d'une station de service qui n'est pas une ConfiguredESSEntity.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ControllerPts extends ESSEntity<Long, User> {
    private static final long serialVersionUID = -6237654525524667183L;

    /**
     * In each of the messages sent to the remote server PTS-2 controller adds “PtsId” field,
     * which is a unique identifier of PTS-2 controller (string, up to 24 hexadecimal digits)
     */
    @Column(nullable = false, unique = true)
    private String ptsId;
    @OneToMany(mappedBy = "controllerPts", cascade = CascadeType.ALL)
    @JsonIgnore // Add this annotation to break the loop
    private Set<ControllerPtsConfiguration> controllerPtsConfigurations;
    @Enumerated(EnumType.STRING)
    private EnumControllerType controllerType;
    private String phone;
    @ManyToOne
    private ControllerPtsConfiguration currentConfiguration;
    @Column(name = "current_configuration_id", insertable = false, updatable = false)
    private Long currentConfigurationId;
    /**
     * Un contrôleur peut avoir plusieurs versions de firmware installées dans le temps
     */
    @OneToMany(mappedBy = "controllerPts")
    private Set<FirmwareInformation> firmwareInformations;

    @ManyToOne
    private FirmwareInformation currentFirmwareInformation;
    @Column(name = "current_firmware_information_id", insertable = false, updatable = false)
    private Long currentFirmwareInformationId;

    /**
     * C'est l'utilisateur avec lequel le contrôleur s'authentifie auprès du serveur pour les upload.
     * Dans le cas des échanges WSS (websocket), pas d'authentification. Mais, il sera utilisé pour les traces
     */
    @ManyToOne(cascade = CascadeType.ALL)
    private User userController;
    @Column(name = "user_controller_id", insertable = false, updatable = false)
    private Long userControllerId;

    @OneToOne
    @JoinColumn(name = "station_id")
    private Station station;
    @Column(name = "station_id", insertable = false, updatable = false)
    private Long stationId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ControllerPts that = (ControllerPts) o;

        return ptsId.equals(that.ptsId);
    }

    @Override
    public int hashCode() {
        return ptsId.hashCode();
    }

    public void attachStation(Station station) {
        this.setStation(station);
    }

    /**
     * Ajout d'une nouvelle configuration
     */
    public ControllerPtsConfiguration addNewConfiguration(String configurationId) {
        final ControllerPtsConfiguration newConfiguration = ControllerPtsConfiguration.initialize(this, configurationId);
        getControllerPtsConfigurations().add(newConfiguration);
        return newConfiguration;
    }
}
