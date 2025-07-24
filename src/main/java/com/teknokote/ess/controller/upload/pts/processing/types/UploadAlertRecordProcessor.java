package com.teknokote.ess.controller.upload.pts.processing.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.controller.upload.pts.processing.base.KnownConfigurationUploadProcessor;
import com.teknokote.ess.core.model.Alert;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.service.impl.AlertService;
import com.teknokote.ess.core.service.impl.MailingService;
import com.teknokote.pts.client.upload.alert.UploadAlertRecordRequestPacket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UploadAlertRecordProcessor extends KnownConfigurationUploadProcessor
{
    @Autowired
    private AlertService alertService;

    @Autowired
    private MailingService mailingService;

    @Value("${ess.mailing.enabled}")
    private Boolean sendAlerts;

    @Override
    protected void doProcess(String uploadedBody, ControllerPtsConfiguration controllerPtsConfiguration) throws JsonProcessingException {
        UploadAlertRecordRequestPacket query = new ObjectMapper().readValue(uploadedBody, UploadAlertRecordRequestPacket.class);
        final List<Alert> receivedAlerts = alertService.saveUploadedAlert(query, controllerPtsConfiguration);
        // Access the station from the controllerPtsConfiguration
        Station station = controllerPtsConfiguration.getControllerPts().getStation();
        // Now you can use the station variable
        if(Boolean.TRUE.equals(sendAlerts))
        {
            receivedAlerts.forEach(alert -> mailingService.sendAlert(alert, station));
        }
    }

    @Override
    public String getKey()
    {
        return "UploadAlertRecord";
    }
}
