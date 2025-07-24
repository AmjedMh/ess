package com.teknokote.ess.controller.upload.pts.processing.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.controller.upload.pts.processing.base.KnownConfigurationUploadProcessor;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.service.impl.TankMeasurementServices;
import com.teknokote.pts.client.upload.dto.UploadMeasurementDto;
import com.teknokote.pts.client.upload.tank.UploadTankMeasurementRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UploadTankMeasurementProcessor extends KnownConfigurationUploadProcessor
{

    @Autowired
    private TankMeasurementServices measurementServices;

    @Override
    protected void doProcess(String uploadedBody, ControllerPtsConfiguration controllerPtsConfiguration) throws JsonProcessingException {
        UploadTankMeasurementRequest query = new ObjectMapper().readValue(uploadedBody, UploadTankMeasurementRequest.class);
        UploadMeasurementDto uploadMeasurementDto =     UploadMeasurementDto.fromUploadTankMeasurement(query);
        measurementServices.processUploadedTankMeasurement(uploadMeasurementDto, controllerPtsConfiguration);
    }

    @Override
    public String getKey()
    {
        return "UploadTankMeasurement";
    }
}
