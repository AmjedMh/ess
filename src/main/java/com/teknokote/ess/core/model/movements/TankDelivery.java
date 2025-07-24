package com.teknokote.ess.core.model.movements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.EnumDeliveryStatus;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.pts.client.upload.dto.UploadSource;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TankDelivery extends ESSEntity<Long, User> {

    @ManyToOne
    @JsonIgnore
    public Tank tank;
    private LocalDateTime dateTime;
    private BigDecimal productHeight;
    private double waterHeight;
    private BigDecimal temperature;
    private double productVolume;
    private double productTCVolume;
    private double pumpsDispensedVolume;
    private String configurationId;
    private double startProductVolume;
    private double endProductVolume;
    private BigDecimal startProductHeight;
    private BigDecimal endProductHeight;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String duration;
    private Double salesVolume;
    @Enumerated(EnumType.STRING)
    private EnumDeliveryStatus status;
    @Enumerated(EnumType.STRING)
    private UploadSource uploadSource;
    @ManyToOne
    @JsonIgnore
    public ControllerPts controllerPts;
}
