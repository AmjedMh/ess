package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.PumpPort;
import com.teknokote.ess.core.repository.PumpPortRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PumpPortServiceTest {

    @InjectMocks
    private PumpPortService pumpPortService;
    @Mock
    private PumpPortRepository pumpPortRepository;
    private PumpPort pumpPort;
    private ControllerPtsConfiguration controllerPtsConfiguration;

    @BeforeEach
    void setUp() {
        pumpPort = new PumpPort();
        pumpPort.setId(1L);

        controllerPtsConfiguration = new ControllerPtsConfiguration();
        controllerPtsConfiguration.setConfigurationId("dd3686gj");
    }

    @Test
    void findAllByIdConfiguredAndControllerPtsConfiguration_ShouldReturnPumpPort_WhenFound() {
        // Given
        Long idConf = 1L;
        when(pumpPortRepository.findAllByIdConfiguredAndControllerPtsConfiguration(idConf, controllerPtsConfiguration))
                .thenReturn(Optional.of(pumpPort));

        // When
        PumpPort result = pumpPortService.findAllByIdConfiguredAndControllerPtsConfiguration(idConf, controllerPtsConfiguration);

        // Then
        assertNotNull(result);
        assertEquals(pumpPort.getId(), result.getId());
        verify(pumpPortRepository, times(1)).findAllByIdConfiguredAndControllerPtsConfiguration(idConf, controllerPtsConfiguration);
    }

    @Test
    void findAllByIdConfiguredAndControllerPtsConfiguration_ShouldReturnNull_WhenNotFound() {
        // Given
        Long idConf = 1L;
        when(pumpPortRepository.findAllByIdConfiguredAndControllerPtsConfiguration(idConf, controllerPtsConfiguration))
                .thenReturn(Optional.empty());

        // When
        PumpPort result = pumpPortService.findAllByIdConfiguredAndControllerPtsConfiguration(idConf, controllerPtsConfiguration);

        // Then
        assertNull(result);
        verify(pumpPortRepository, times(1)).findAllByIdConfiguredAndControllerPtsConfiguration(idConf, controllerPtsConfiguration);
    }
}