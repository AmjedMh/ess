package com.teknokote.ess.core.service.cache;

import com.teknokote.core.config.MapperConfiguration;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.model.movements.TankMeasurement;
import com.teknokote.ess.dto.TankMeasurementsDto;
import com.teknokote.pts.client.upload.dto.MeasurementDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface MeasurementMapper {

   @Mapping(target = "tank", expression = "java(tank.getIdConf())")
   @Mapping(target = "fuelGradeId", expression = "java(tank.getGrade().getIdConf())")
   @Mapping(target = "fullGrade", expression = "java(tank.getGrade().getName())")
   @Mapping(target = "configurationId", expression = "java(controllerPtsConfiguration.getConfigurationId())")
   @Mapping(target = "controllerPts", expression = "java(controllerPtsConfiguration.getControllerPts())")
   @Mapping(target = "controllerPtsConfiguration", expression = "java(controllerPtsConfiguration)")
   @Mapping(target = "version", constant = "0L")
   @Mapping(target = "status", constant = "OK")
   @Mapping(target = "productHeight", defaultValue = "0.0")
   @Mapping(target = "temperature", defaultValue = "0.0")
   @Mapping(target = "waterHeight", defaultValue = "0.0")
   @Mapping(target = "productVolume", defaultValue = "0.0")
   @Mapping(target = "waterVolume", defaultValue = "0.0")
   @Mapping(target = "productUllage", defaultValue = "0.0")
   @Mapping(target = "productDensity", defaultValue = "0.0")
   @Mapping(target = "productMass", defaultValue = "0.0")
   @Mapping(target = "productTCVolume", constant = "0.0")
   TankMeasurement toTankMeasurement(MeasurementDto measurement, @Context Tank tank, @Context ControllerPtsConfiguration controllerPtsConfiguration);

   @Mapping(target = "tankFillingPercentage", expression = "java(calculateTankFillingPercentage(tankMeasurement))")
   TankMeasurementsDto toDto(TankMeasurement tankMeasurement);

   @Mapping(target = "tankFillingPercentage", expression = "java(calculateTankFillingPercentage(tankMeasurement))")
   MeasurementDto mapToMeasurementDto(TankMeasurement tankMeasurement);

   default double calculateTankFillingPercentage(TankMeasurement tankMeasurement) {
      // Ensure the denominator is not zero before division
      double productVolume = tankMeasurement.getProductVolume() != null ? tankMeasurement.getProductVolume() : 0.0;
      double waterVolume = tankMeasurement.getWaterVolume() != null ? tankMeasurement.getWaterVolume() : 0.0;
      double productUllage = tankMeasurement.getProductUllage() != null ? tankMeasurement.getProductUllage() : 0.0;

      double totalVolume = productVolume + waterVolume + productUllage;

      if (totalVolume == 0.0) {
         return 0.0; // Avoid division by zero
      }

      double percentage = ((productVolume + waterVolume) / totalVolume) * 100;
      return Math.round(percentage);
   }
}
