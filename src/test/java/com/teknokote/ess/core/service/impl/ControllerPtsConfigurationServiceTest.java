package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.repository.configuration.ControllerPtsConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ControllerPtsConfigurationServiceTest {

    @InjectMocks
    private ControllerPtsConfigurationService controllerPtsConfigurationService;

    @Mock
    private ControllerPtsConfigurationRepository controllerPtsConfigurationRepository;

    private ControllerPtsConfiguration configuration;

    @BeforeEach
    void setUp() {
        // Sample data for tests
        configuration = new ControllerPtsConfiguration();
        configuration.setId(1L);
        configuration.setPtsId("0027003A3438510935383135");
        configuration.setConfigurationId("150e9c08");
        // Set other properties as needed
    }

    @Test
    void testConfigurationExists_ReturnsTrueWhenExists() {
        when(controllerPtsConfigurationRepository.findByPtsIdAndConfigurationId("0027003A3438510935383135", "150e9c08"))
                .thenReturn(Optional.of(configuration));

        boolean exists = controllerPtsConfigurationService.configurationExists("0027003A3438510935383135", "150e9c08");

        assertTrue(exists);
        verify(controllerPtsConfigurationRepository, times(1))
                .findByPtsIdAndConfigurationId("0027003A3438510935383135", "150e9c08");
    }

    @Test
    void testConfigurationExists_ReturnsFalseWhenNotExists() {
        when(controllerPtsConfigurationRepository.findByPtsIdAndConfigurationId("0027003A3438510935383135", "150e9c08"))
                .thenReturn(Optional.empty());

        boolean exists = controllerPtsConfigurationService.configurationExists("0027003A3438510935383135", "150e9c08");

        assertFalse(exists);
        verify(controllerPtsConfigurationRepository, times(1))
                .findByPtsIdAndConfigurationId("0027003A3438510935383135", "150e9c08");
    }

    @Test
    void testFindByPtsIdAndConfigurationId_ReturnsConfiguration() {
        when(controllerPtsConfigurationRepository.findByPtsIdAndConfigurationId("0027003A3438510935383135", "150e9c08"))
                .thenReturn(Optional.of(configuration));

        ControllerPtsConfiguration result = controllerPtsConfigurationService.findByPtsIdAndConfigurationId("0027003A3438510935383135", "150e9c08");

        assertNotNull(result);
        assertEquals(configuration.getId(), result.getId());
        verify(controllerPtsConfigurationRepository, times(1))
                .findByPtsIdAndConfigurationId("0027003A3438510935383135", "150e9c08");
    }

    @Test
    void testFindByPtsIdAndConfigurationId_ReturnsNullWhenNotFound() {
        when(controllerPtsConfigurationRepository.findByPtsIdAndConfigurationId("0027003A3438510935383135", "150e9c08"))
                .thenReturn(Optional.empty());

        ControllerPtsConfiguration result = controllerPtsConfigurationService.findByPtsIdAndConfigurationId("0027003A3438510935383135", "150e9c08");

        assertNull(result);
        verify(controllerPtsConfigurationRepository, times(1))
                .findByPtsIdAndConfigurationId("0027003A3438510935383135", "150e9c08");
    }

    @Test
    void testFindCurrentConfigurationOnController_ReturnsConfiguration() {
        when(controllerPtsConfigurationRepository.findCurrentConfigurationOnController(1L))
                .thenReturn(Optional.of(configuration));

        ControllerPtsConfiguration result = controllerPtsConfigurationService.findCurrentConfigurationOnController(1L);

        assertNotNull(result);
        assertEquals(configuration.getId(), result.getId());
        verify(controllerPtsConfigurationRepository, times(1))
                .findCurrentConfigurationOnController(1L);
    }

    @Test
    void testFindCurrentConfigurationOnController_ReturnsNullWhenNotFound() {
        when(controllerPtsConfigurationRepository.findCurrentConfigurationOnController(1L))
                .thenReturn(Optional.empty());

        ControllerPtsConfiguration result = controllerPtsConfigurationService.findCurrentConfigurationOnController(1L);

        assertNull(result);
        verify(controllerPtsConfigurationRepository, times(1))
                .findCurrentConfigurationOnController(1L);
    }

    @Test
    void testFindAll_ReturnsListOfConfigurations() {
        when(controllerPtsConfigurationRepository.findAll())
                .thenReturn(Collections.singletonList(configuration));

        List<ControllerPtsConfiguration> result = controllerPtsConfigurationService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(configuration.getId(), result.get(0).getId());
        verify(controllerPtsConfigurationRepository, times(1))
                .findAll();
    }

    @Test
    void testFindById_ReturnsConfigurationWhenFound() {
        when(controllerPtsConfigurationRepository.findById(1L))
                .thenReturn(Optional.of(configuration));

        Optional<ControllerPtsConfiguration> result = controllerPtsConfigurationService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(configuration.getId(), result.get().getId());
        verify(controllerPtsConfigurationRepository, times(1))
                .findById(1L);
    }

    @Test
    void testFindById_ReturnsEmptyWhenNotFound() {
        when(controllerPtsConfigurationRepository.findById(1L))
                .thenReturn(Optional.empty());

        Optional<ControllerPtsConfiguration> result = controllerPtsConfigurationService.findById(1L);

        assertFalse(result.isPresent());
        verify(controllerPtsConfigurationRepository, times(1))
                .findById(1L);
    }
}