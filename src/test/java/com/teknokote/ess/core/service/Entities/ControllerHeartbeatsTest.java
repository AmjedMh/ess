package com.teknokote.ess.core.service.Entities;

import com.teknokote.ess.core.service.cache.ControllerHeartbeats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ControllerHeartbeatsTest {

    private ControllerHeartbeats controllerHeartbeats;

    @BeforeEach
    void setUp() {
        // Initialize the ControllerHeartbeats instance before each test
        controllerHeartbeats = new ControllerHeartbeats();
    }

    @Test
    void testUpdateLastConnectionStoresCorrectTime() {
        // Arrange
        String ptsId = "pts123";
        LocalDateTime connectionTime = LocalDateTime.now();

        // Act
        controllerHeartbeats.updateLastConnection(ptsId, connectionTime);

        // Assert
        LocalDateTime retrievedTime = controllerHeartbeats.getLastConnection(ptsId);
        assertNotNull(retrievedTime);
        assertEquals(connectionTime, retrievedTime);
    }

    @Test
    void testGetLastConnectionReturnsNullForUnknownId() {
        // Act
        LocalDateTime retrievedTime = controllerHeartbeats.getLastConnection("unknownId");

        // Assert
        assertNull(retrievedTime);
    }

    @Test
    void testUpdateLastConnectionOverwritesPreviousTime() {
        // Arrange
        String ptsId = "pts123";
        LocalDateTime firstTime = LocalDateTime.now();
        LocalDateTime secondTime = firstTime.plusHours(1);

        // Act
        controllerHeartbeats.updateLastConnection(ptsId, firstTime);
        controllerHeartbeats.updateLastConnection(ptsId, secondTime);

        // Assert
        LocalDateTime retrievedTime = controllerHeartbeats.getLastConnection(ptsId);
        assertEquals(secondTime, retrievedTime);  // Make sure the latest time is stored
    }
}