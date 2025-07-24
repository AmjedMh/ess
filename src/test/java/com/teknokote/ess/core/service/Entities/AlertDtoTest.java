package com.teknokote.ess.core.service.Entities;

import com.teknokote.ess.core.model.EnumAlertStatus;
import com.teknokote.ess.dto.charts.AlertDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertDtoTest {

    @Test
    void testAlertDtoCreation() {
        // Arrange
        String dateTime = "2023-01-01T12:00:00";
        String deviceType = "Pump";
        Long deviceNumber = 12345L;
        EnumAlertStatus status = EnumAlertStatus.RECIEVED;
        String controllerPtsId = "PTS1234";
        String alertDescription = "Temperature Alert";

        // Act
        AlertDto alertDto = AlertDto.builder()
                .dateTime(dateTime)
                .deviceType(deviceType)
                .deviceNumber(deviceNumber)
                .status(status)
                .controllerPtsId(controllerPtsId)
                .alertDescription(alertDescription)
                .build();

        // Assert
        assertEquals(dateTime, alertDto.getDateTime());
        assertEquals(deviceType, alertDto.getDeviceType());
        assertEquals(deviceNumber, alertDto.getDeviceNumber());
        assertEquals(status, alertDto.getStatus());
        assertEquals(controllerPtsId, alertDto.getControllerPtsId());
        assertEquals(alertDescription, alertDto.getAlertDescription());
    }
}