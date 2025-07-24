package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.events.types.CardOnReaderEvent;
import com.teknokote.pts.client.upload.status.UploadStatusRequest;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardOnReaderEventTest {

    @Test
    void testCardOnReaderEventCreation() {
        // Arrange
        Object source = new Object();
        UploadStatusRequest uploadStatusRequest = new UploadStatusRequest();
        ControllerPtsConfiguration controllerPtsConfiguration = new ControllerPtsConfiguration(); // Instantiate your ControllerPtsConfiguration (or mock it if needed)

        // Act
        CardOnReaderEvent event = CardOnReaderEvent.of(source, uploadStatusRequest, controllerPtsConfiguration);

        // Assert
        assertEquals(source, event.getSource());
        assertEquals(uploadStatusRequest, event.getUploadStatusRequest());
        assertEquals(controllerPtsConfiguration, event.getControllerPtsConfiguration());
    }
}