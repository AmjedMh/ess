package com.teknokote.ess.dto;

import com.teknokote.ess.core.model.EnumDeliveryStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class TankDeliveryDto {

    private Long tank;
    private LocalDateTime dateTime;
    private String fuelGradeName;
    private double waterHeight;
    private BigDecimal temperature;
    private double productTCVolume;
    private double productDensity;
    private double productMass;
    private double pumpsDispensedVolume;
    private double startProductVolume;
    private double endProductVolume;
    private double productVolume;
    private BigDecimal startProductHeight;
    private BigDecimal endProductHeight;
    private BigDecimal productHeight;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String duration;
    private EnumDeliveryStatus status;
    private Double salesVolume;
}
