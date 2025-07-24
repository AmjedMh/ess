package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.Alert;
import com.teknokote.ess.core.model.EnumAlertStatus;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.repository.AlertRepository;
import com.teknokote.ess.dto.charts.AlertDto;
import com.teknokote.pts.client.upload.alert.UploadAlertRecord;
import com.teknokote.pts.client.upload.alert.UploadAlertRecordPacket;
import com.teknokote.pts.client.upload.alert.UploadAlertRecordRequestPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AlertService {
    @Autowired
    private AlertRepository alertRepository;
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    public List<Alert> saveUploadedAlert(UploadAlertRecordRequestPacket recordPacket, ControllerPtsConfiguration controllerPtsConfiguration) {

        List<Alert> alerts=new ArrayList<>();
        for (UploadAlertRecordPacket requestPacket : recordPacket.getPackets()) {
            alerts.add(add(requestPacket.getData(), controllerPtsConfiguration));
        }
        return alerts;
    }

    public Alert add(UploadAlertRecord alertRecord, ControllerPtsConfiguration controllerPtsConfiguration) {
        Alert alert = new Alert();
        alert.setDateTime(alertRecord.getDateTime());
        alert.setDeviceType(alertRecord.getDeviceType());
        alert.setDeviceNumber(alertRecord.getDeviceNumber());
        alert.setState(alertRecord.getState());
        alert.setCode(alertRecord.getCode());
        alert.setConfigurationId(alertRecord.getConfigurationId());
        ControllerPts controllerPts = controllerPtsConfiguration.getControllerPts();
        alert.setControllerPts(controllerPts);
        alert.setAlertDescription(Alert.DeviceType.valueOf(alertRecord.getDeviceType()), alertRecord.getCode());
        alert.setStatus(EnumAlertStatus.RECIEVED);
        simpMessagingTemplate.convertAndSend("/topic/alerts", alertToDto(alert));
        return alertRepository.save(alert);
    }

    public void treatAlert(Alert alert){
        alertRepository.save(alert);

    }

    private AlertDto alertToDto(Alert alert){
        return AlertDto.builder()
                .dateTime(alert.getDateTime())
                .deviceNumber(alert.getDeviceNumber())
                .alertDescription(alert.getAlertDescription())
                .controllerPtsId(alert.getControllerPts().getPtsId())
                .deviceType(alert.getDeviceType())
                .status(alert.getStatus()).build();
    }
}