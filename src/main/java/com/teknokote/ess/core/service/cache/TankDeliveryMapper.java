package com.teknokote.ess.core.service.cache;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.ess.core.model.EnumDeliveryStatus;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.model.movements.TankDelivery;
import com.teknokote.ess.dto.TankDeliveryDto;
import com.teknokote.pts.client.upload.dto.MeasurementDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper(config = MapperConfiguration.class)
public interface TankDeliveryMapper {
    @Mapping(target = "tank", expression = "java(tank)")
    @Mapping(target = "controllerPts", expression = "java(controllerPtsConfiguration.getControllerPts())")
    @Mapping(target = "productHeight", expression = "java(toBigDecimal(measurement.getProductHeight() - previousMeasurement.getProductHeight()))")
    @Mapping(target = "temperature", expression = "java(toBigDecimal(measurement.getTemperature()))")
    @Mapping(target = "waterHeight", expression = "java(measurement.getWaterHeight())")
    @Mapping(target = "pumpsDispensedVolume", constant = "0.0")
    @Mapping(target = "productVolume", expression = "java(measurement.getProductVolume() - previousMeasurement.getProductVolume())")
    @Mapping(target = "configurationId", expression = "java(controllerPtsConfiguration.getConfigurationId())")
    @Mapping(target = "startProductVolume", expression = "java(previousMeasurement.getProductVolume())")
    @Mapping(target = "endProductVolume", expression = "java(measurement.getProductVolume())")
    @Mapping(target = "startProductHeight", expression = "java(toBigDecimal(previousMeasurement.getProductHeight()))")
    @Mapping(target = "endProductHeight", expression = "java(toBigDecimal(measurement.getProductHeight()))")
    @Mapping(target = "dateTime", expression = "java(measurement.getDateTime())")
    @Mapping(target = "startDateTime", expression = "java(previousMeasurement.getDateTime())")
    @Mapping(target = "endDateTime", source = "dateTime")
    @Mapping(target = "status", source = "status")
    TankDelivery measurementToDelivery(MeasurementDto measurement, MeasurementDto previousMeasurement, @Context Tank tank, @Context ControllerPtsConfiguration controllerPtsConfiguration, LocalDateTime dateTime, EnumDeliveryStatus status);
    default BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value);
    }
    @Mapping(target = "tank", source = "tank.idConf")
    @Mapping(target = "fuelGradeName", source = "tank.grade.name")
    TankDeliveryDto toDto (TankDelivery tankDelivery);
}