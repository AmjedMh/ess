package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.Alert;
import com.teknokote.ess.core.model.EnumAlertStatus;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.repository.AlertRepository;
import com.teknokote.pts.client.upload.alert.UploadAlertRecord;
import com.teknokote.pts.client.upload.alert.UploadAlertRecordPacket;
import com.teknokote.pts.client.upload.alert.UploadAlertRecordRequestPacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.List;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @InjectMocks
    private AlertService alertService;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    private ControllerPtsConfiguration controllerPtsConfiguration;

    @BeforeEach
    void setUp() {
        // Initialization of mocks happens automatically with @ExtendWith
        controllerPtsConfiguration = new ControllerPtsConfiguration();
        ControllerPts controllerPts = new ControllerPts();
        controllerPts.setPtsId("PTS123");
        controllerPtsConfiguration.setControllerPts(controllerPts);
    }

    @Test
    void testSaveUploadedAlert() {
        UploadAlertRecordRequestPacket recordPacket = new UploadAlertRecordRequestPacket();
        UploadAlertRecord uploadAlertRecord = new UploadAlertRecord();

        // Set properties correctly
        uploadAlertRecord.setDateTime("2023-01-01T12:00:00");
        uploadAlertRecord.setDeviceType("PUMP");
        uploadAlertRecord.setDeviceNumber(12345L);
        uploadAlertRecord.setState("Active");
        uploadAlertRecord.setCode(1L);
        uploadAlertRecord.setConfigurationId("CONFIG123");

        UploadAlertRecordPacket packet = new UploadAlertRecordPacket();
        packet.setData(uploadAlertRecord);
        recordPacket.setPackets(Collections.singletonList(packet));

        // Mock the repository's save method to return the alert
        when(alertRepository.save(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        // Act
        List<Alert> alerts = alertService.saveUploadedAlert(recordPacket, controllerPtsConfiguration);

        // Assert
        assertEquals(1, alerts.size());
        Alert alert = alerts.get(0);
        assertEquals("2023-01-01T12:00:00", alert.getDateTime());
        assertEquals("PUMP", alert.getDeviceType());
        assertEquals(12345L, alert.getDeviceNumber());
        assertEquals("PTS123", alert.getControllerPts().getPtsId());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void testAdd() {
        UploadAlertRecord uploadAlertRecord = new UploadAlertRecord();
        uploadAlertRecord.setDateTime("2023-01-01T12:00:00");
        uploadAlertRecord.setDeviceType("PUMP"); // Ensure correct case
        uploadAlertRecord.setDeviceNumber(12345L);
        uploadAlertRecord.setState("Active");
        uploadAlertRecord.setCode(1L);
        uploadAlertRecord.setConfigurationId("CONFIG123");

        // Create a mock alert to return when save is called
        Alert mockAlert = new Alert();
        mockAlert.setDateTime("2023-01-01T12:00:00");
        mockAlert.setDeviceType("PUMP");
        mockAlert.setDeviceNumber(12345L);
        mockAlert.setControllerPts(controllerPtsConfiguration.getControllerPts());
        mockAlert.setStatus(EnumAlertStatus.RECIEVED);

        // Mock the alertRepository's save method to return the mock alert
        when(alertRepository.save(any(Alert.class))).thenReturn(mockAlert);

        // Act
        Alert alert = alertService.add(uploadAlertRecord, controllerPtsConfiguration);

        // Assert
        assertNotNull(alert);
        assertEquals("2023-01-01T12:00:00", alert.getDateTime());
        assertEquals("PUMP", alert.getDeviceType());
        assertEquals(12345L, alert.getDeviceNumber());
        assertEquals("PTS123", alert.getControllerPts().getPtsId());
        assertEquals(EnumAlertStatus.RECIEVED, alert.getStatus());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void testTreatAlert() {
        Alert alert = new Alert();
        alert.setDateTime("2023-01-01T12:00:00");

        // Act
        alertService.treatAlert(alert);

        // Assert
        verify(alertRepository, times(1)).save(alert);
    }
}