package com.teknokote.ess.controller.upload.pts.processing.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.controller.upload.pts.processing.base.KnownConfigurationUploadProcessor;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.service.impl.TankMeasurementServices;
import com.teknokote.ess.events.listeners.PumpStatusService;
import com.teknokote.ess.events.types.CardOnReaderEvent;
import com.teknokote.pts.client.upload.dto.UploadMeasurementDto;
import com.teknokote.pts.client.upload.status.UploadStatusRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class UploadStatusProcessor extends KnownConfigurationUploadProcessor
{
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private PumpStatusService pumpStatusService;
    @Autowired
    private TankMeasurementServices measurementServices;

    @Override
    protected void doProcess(String uploadedBody, ControllerPtsConfiguration controllerPtsConfiguration) throws JsonProcessingException {
        UploadStatusRequest query = new ObjectMapper().readValue(uploadedBody, UploadStatusRequest.class);

        pumpStatusService.pumpStationStatus(query.getPackets().get(0).getData(),controllerPtsConfiguration);

        UploadMeasurementDto uploadMeasurementDto = UploadMeasurementDto.fromUploadStatus(query);
        measurementServices.processUploadedTankMeasurement(uploadMeasurementDto, controllerPtsConfiguration);

        if(uploadedBody.contains("Tags")){
            applicationEventPublisher.publishEvent(CardOnReaderEvent.of(this,query,controllerPtsConfiguration));
        }
    }

    @Override
    public String getKey()
    {
        return "UploadStatus";
    }
}
