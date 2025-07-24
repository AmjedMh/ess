package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.scheduling.TaskExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class TaskExecutionTest {

    private AtomicBoolean verrou;

    @BeforeEach
    void setUp() {
        verrou = new AtomicBoolean(true); // Initially set to true
    }

    @Test
    void testCloseSetsVerrouToFalse() {
        // Arrange
        try (TaskExecution taskExecution = TaskExecution.of(verrou)) {
            // Action is performed within the try-with-resources statement
        }

        // Assert
        assertFalse(verrou.get(), "verrou should be set to false after closing TaskExecution");
    }

    @Test
    void testCloseMethod() {
        // Arrange
        TaskExecution taskExecution = TaskExecution.of(verrou);

        // Act
        taskExecution.close();

        // Assert
        assertFalse(verrou.get(), "verrou should be set to false after explicitly calling close");
    }
}