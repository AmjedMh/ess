package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.configuration.PumpPort;
import com.teknokote.ess.core.repository.PumpPortRepository;
import com.teknokote.ess.core.repository.PumpRepository;
import com.teknokote.ess.dto.PumpsConfigDto;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSPump;
import com.teknokote.pts.client.response.configuration.PTSPumpPort;
import com.teknokote.pts.client.response.configuration.PTSPumpsConfigurationResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSPumpsConfigurationResponsePacketData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PumpServiceTest {

    @Mock
    private PumpRepository pumpRepository;
    @Mock
    private PumpPortRepository pumpPortRepository;
    @Mock
    private PumpPortService pumpPortService;
    @Mock
    private ControllerPtsConfigurationService controllerPtsConfigurationService;
    @InjectMocks
    private PumpService pumpService;
    @Mock
    private Pump pump;
    @Mock
    private PumpPort pumpPort;

    @BeforeEach
    void setUp() {
        // Set up a Pump instance for testing
        pump = new Pump();
        pump.setIdConf(1L);
        pump.setAddress(1L);
        pump.setPort(new PumpPort());
        pump.getPort().setIdConfigured(123L);
        pump.getPort().setProtocol(1L);
        pump.getPort().setBaudRate(9600L);
    }
    @Test
    void testUpdatePump() {
        // Given
        Long pumpId = 1L;
        Pump updatedPump = new Pump();
        updatedPump.setPort(new PumpPort());
        updatedPump.getPort().setIdConfigured(456L);
        updatedPump.getPort().setProtocol(2L);
        updatedPump.getPort().setBaudRate(115200L);

        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(pump));
        when(pumpRepository.save(pump)).thenReturn(pump);
        // When
        pumpService.update(pumpId, updatedPump);

        // Then
        verify(pumpRepository, times(1)).findById(pumpId);
        verify(pumpRepository, times(1)).save(pump);
        assertEquals(456L, pump.getPort().getIdConfigured());
        assertEquals(2L, pump.getPort().getProtocol());
        assertEquals(115200L, pump.getPort().getBaudRate());
    }

    @Test
    void testUpdatePumpNotFound() {
        // Given
        Long pumpId = 99L; // A non-existent id
        Pump updatedPump = new Pump();

        when(pumpRepository.findById(pumpId)).thenReturn(Optional.empty());

        // When
        pumpService.update(pumpId, updatedPump);

        // Then
        verify(pumpRepository, times(1)).findById(pumpId);
        verify(pumpRepository, never()).save(any());
    }

    @Test
    void testDeletePump() {
        // Given
        Long pumpId = 1L;
        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(pump));

        // When
        pumpService.delete(pumpId);

        // Then
        verify(pumpRepository, times(1)).findById(pumpId);
        verify(pumpRepository, times(1)).delete(pump);
    }

    @Test
    void testDeletePumpNotFound() {
        // Given
        Long pumpId = 99L; // A non-existent id
        when(pumpRepository.findById(pumpId)).thenReturn(Optional.empty());

        // When
        pumpService.delete(pumpId);

        // Then
        verify(pumpRepository, times(1)).findById(pumpId);
        verify(pumpRepository, never()).delete(any());
    }

    @Test
    void testMapToPumpConfigDto() {
        // When
        PumpsConfigDto pumpsConfigDto = pumpService.mapToPumpConfigDto(pump);

        // Then
        assertNotNull(pumpsConfigDto);
        assertEquals(1L, pumpsConfigDto.getId());
        assertEquals(1L, pumpsConfigDto.getProtocol());
        assertEquals(9600L, pumpsConfigDto.getBaudRate());
    }

    @Test
    void testFindPumpsByControllerOnCurrentConfiguration() {
        Long idCtr = 1L;
        ControllerPtsConfiguration configuration = new ControllerPtsConfiguration();
        List<Pump> expectedPumps = new ArrayList<>();
        expectedPumps.add(new Pump());

        // Mock the behavior of the service and repository
        when(controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr)).thenReturn(configuration);
        when(pumpRepository.findPumpsByControllerConfiguration(configuration)).thenReturn(expectedPumps);

        // When
        List<Pump> result = pumpService.findPumpsByControllerOnCurrentConfiguration(idCtr);

        // Then
        assertNotNull(result);
        assertEquals(expectedPumps.size(), result.size());
        assertEquals(expectedPumps.get(0), result.get(0));
    }

    @Test
    void testAddNewPumpPortC() {
        // Mock setup
        ControllerPtsConfiguration configuration = new ControllerPtsConfiguration();
        PTSPumpsConfigurationResponsePacket responsePacket = mock(PTSPumpsConfigurationResponsePacket.class);
        PTSPumpsConfigurationResponsePacketData data = mock(PTSPumpsConfigurationResponsePacketData.class);
        PTSPumpPort ptsPumpPort = mock(PTSPumpPort.class);

        List<PTSPumpPort> ports = Collections.singletonList(ptsPumpPort);
        List<PTSPump> pumps = Collections.emptyList(); // Simulate no pumps for this case

        // Mocking behavior
        when(responsePacket.getData()).thenReturn(data);
        when(data.getPorts()).thenReturn(ports);
        when(data.getPumps()).thenReturn(pumps);

        // Mocking packet processing
        JsonPTSResponse response = mock(JsonPTSResponse.class);
        when(response.getPackets()).thenReturn(Collections.singletonList(responsePacket));

        // Perform the operation
        pumpService.addNewPumpPortC(configuration, response);

        // Verify that addPort was called for each port
        verify(pumpPortRepository, times(1)).save(any(PumpPort.class));
        // No calls to addPump should be made as there are no pumps
        verify(pumpRepository, never()).save(any(Pump.class));
    }

    @Test
    void testAddPump() {
        // Given
        ControllerPtsConfiguration configuration = new ControllerPtsConfiguration();
        PTSPump ptspump = mock(PTSPump.class);
        when(ptspump.getId()).thenReturn(1L);
        when(ptspump.getPort()).thenReturn(1L);
        when(ptspump.getAddress()).thenReturn(1L);

        // Mock repository behavior
        when(pumpRepository.findAllByIdConfAndControllerPtsConfiguration(1L, configuration)).thenReturn(null);
        when(pumpPortService.findAllByIdConfiguredAndControllerPtsConfiguration(1L, configuration)).thenReturn(pumpPort);
        when(pumpRepository.save(any(Pump.class))).thenReturn(new Pump());

        // When
        Pump result = pumpService.addPump(ptspump, configuration);

        // Then
        assertNotNull(result);
        verify(pumpRepository, times(1)).save(any(Pump.class));
    }

    @Test
    void testGetAll() {
        // Given
        List<Pump> expectedPumps = new ArrayList<>();
        expectedPumps.add(new Pump());

        // Mock repository behavior
        when(pumpRepository.findAll()).thenReturn(expectedPumps);

        // When
        List<Pump> result = pumpService.getAll();

        // Then
        assertNotNull(result);
        assertEquals(expectedPumps.size(), result.size());
        assertEquals(expectedPumps, result);
    }

    @Test
    void testFindPumpIdsByControllerConfiguration() {
        // Mock data
        String controllerPtsConfiguration = "config123";
        String ptsId = "pts123";
        List<Long> expectedIds = List.of(1L, 2L, 3L);

        // Mock repository behavior
        when(pumpRepository.findPumpIdsByControllerConfiguration(controllerPtsConfiguration, ptsId)).thenReturn(expectedIds);

        // When
        List<Long> resultIds = pumpService.findPumpIdsByControllerConfiguration(controllerPtsConfiguration, ptsId);

        // Then
        assertNotNull(resultIds);
        assertEquals(expectedIds.size(), resultIds.size());
        assertIterableEquals(expectedIds, resultIds);
    }

    @Test
    void testFindByIdConfAndConfigurationIdAndControllerId() {
        // Mock data
        String configurationId = "config123";
        String ptsId = "pts456";
        Long idConf = 1L;
        Pump expectedPump = new Pump();

        // Mock repository behavior
        when(pumpRepository.findByIdConfAndConfigurationIdAndControllerId(configurationId, ptsId, idConf)).thenReturn(expectedPump);

        // When
        Pump resultPump = pumpService.findByIdConfAndConfigurationIdAndControllerId(configurationId, ptsId, idConf);

        // Then
        assertNotNull(resultPump);
        assertEquals(expectedPump, resultPump);
    }

    @Test
    void testFindAllByIdConfAndControllerPtsConfiguration() {
        // Mock data
        Long idConf = 1L;
        ControllerPtsConfiguration controllerPtsConfiguration = new ControllerPtsConfiguration();
        Pump expectedPump = new Pump();

        // Mock repository behavior
        when(pumpRepository.findAllByIdConfAndControllerPtsConfiguration(idConf, controllerPtsConfiguration)).thenReturn(expectedPump);

        // When
        Pump resultPump = pumpService.findAllByIdConfAndControllerPtsConfiguration(idConf, controllerPtsConfiguration);

        // Then
        assertNotNull(resultPump);
        assertEquals(expectedPump, resultPump);
    }

    @Test
    void testFindByIdConfAndControllerPtsConfiguration() {
        // Mock data
        Long idConf = 1L;
        ControllerPtsConfiguration configuration = new ControllerPtsConfiguration();
        Pump expectedPump = new Pump();

        // Mock repository behavior
        when(pumpRepository.findByIdConfAndControllerPtsConfiguration(idConf, configuration)).thenReturn(Optional.of(expectedPump));

        // When
        Optional<Pump> resultPumpOpt = pumpService.findByIdConfAndControllerPtsConfiguration(idConf, configuration);

        // Then
        assertTrue(resultPumpOpt.isPresent());
        assertEquals(expectedPump, resultPumpOpt.get());
    }
}