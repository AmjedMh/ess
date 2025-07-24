package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Probe;
import com.teknokote.ess.core.model.configuration.ProbePort;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.repository.ControllePtsRepository;
import com.teknokote.ess.core.repository.ProbePortRepository;
import com.teknokote.ess.core.repository.ProbeRepository;
import com.teknokote.ess.dto.ProbeConfigDto;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSProbe;
import com.teknokote.pts.client.response.configuration.PTSProbePort;
import com.teknokote.pts.client.response.configuration.PTSProbesConfigurationResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSProbesConfigurationResponsePacketData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProbeServiceTest {
    @InjectMocks
    private ProbeService probeService;
    @Mock
    private ProbeRepository probeRepository;
    @Mock
    private ProbePortRepository probePortRepository;
    @Mock
    private TankService tankService;
    @Mock
    private ControllePtsRepository controllePtsRepository;
    @Mock
    private ControllerPtsConfigurationService controllerPtsConfigurationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void update_ShouldUpdateProbe() {
        Long probeId = 1L;
        Probe existingProbe = new Probe();
        existingProbe.setAddress(1L);

        when(probeRepository.findById(probeId)).thenReturn(Optional.of(existingProbe));

        Probe newProbeData = new Probe();
        newProbeData.setAddress(2L);

        probeService.update(probeId, newProbeData);

        assertEquals(2L, existingProbe.getAddress());
        verify(probeRepository, times(1)).save(existingProbe);
    }

    @Test
    void findProbesByControllerOnCurrentConfiguration_ShouldReturnProbes() {
        Long idCtr = 1L;
        ControllerPtsConfiguration configuration = mock(ControllerPtsConfiguration.class);
        Probe probe = new Probe();

        when(controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr)).thenReturn(configuration);
        when(probeRepository.findProbesByControllerOnCurrentConfiguration(configuration)).thenReturn(List.of(probe));

        List<Probe> result = probeService.findProbesByControllerOnCurrentConfiguration(idCtr);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(probe, result.get(0));
    }

    @Test
    void delete_ShouldRemoveProbe() {
        Long probeId = 1L;
        Probe probe = new Probe();

        when(probeRepository.findById(probeId)).thenReturn(Optional.of(probe));

        probeService.delete(probeId);

        verify(probeRepository, times(1)).delete(probe);
    }

    @Test
    void addNewProbePort_ShouldReturnResponse() {
        JsonPTSResponse response = new JsonPTSResponse();
        JsonPTSResponse result = probeService.addNewProbePort(response);

        assertSame(result, response);
    }
    @Test
    void addProbes_ShouldAddProbesAndPorts() {
        ControllerPtsConfiguration configuration = mock(ControllerPtsConfiguration.class);
        JsonPTSResponse response = mock(JsonPTSResponse.class);
        PTSProbesConfigurationResponsePacket packet = mock(PTSProbesConfigurationResponsePacket.class);
        PTSProbesConfigurationResponsePacketData data = mock(PTSProbesConfigurationResponsePacketData.class);
        PTSProbe probe = mock(PTSProbe.class);
        PTSProbePort port = mock(PTSProbePort.class);

        when(response.getPackets()).thenReturn(List.of(packet));
        when(packet.getData()).thenReturn(data);
        when(data.getProbes()).thenReturn(List.of(probe));
        when(data.getPorts()).thenReturn(List.of(port));

        when(port.getId()).thenReturn("1");
        when(probe.getId()).thenReturn(1L);

        probeService.addProbes(configuration, response);

        // Verify if repository methods were called with expected arguments
        verify(probePortRepository, times(1)).findAllByIdConfiguredAndControllerPtsConfiguration("1", configuration);
        verify(probeRepository, times(1)).findAllByIdConfAndControllerPtsConfiguration(1L, configuration);
    }

    @Test
    void addPort_ShouldCreateOrUpdateProbePort() {
        // Arrange
        ControllerPtsConfiguration configuration = mock(ControllerPtsConfiguration.class);
        PTSProbePort portPb = mock(PTSProbePort.class);
        when(portPb.getId()).thenReturn("1");

        when(probePortRepository.findAllByIdConfiguredAndControllerPtsConfiguration("1", configuration))
                .thenReturn(Optional.empty());

        // Act
        probeService.addPort(portPb, configuration);

        // Assert
        ArgumentCaptor<ProbePort> captor = ArgumentCaptor.forClass(ProbePort.class);
        verify(probePortRepository, times(1)).save(captor.capture());

        ProbePort savedPort = captor.getValue();
        assertNotNull(savedPort);
        assertEquals("1", savedPort.getIdConfigured());
    }

    @Test
    void addProbe_ShouldCreateOrUpdateProbe() {
        // Arrange
        ControllerPtsConfiguration configuration = mock(ControllerPtsConfiguration.class);
        PTSProbe probeData = new PTSProbe();
        probeData.setId(1L);

        Tank tank = new Tank();
        tank.setId(1L);
        tank.setIdConf(1L);

        ProbePort port = new ProbePort();
        port.setIdConfigured("1");

        Probe probe = new Probe();
        probe.setIdConf(1L);
        probe.setId(1L);
        probe.setTank(tank);
        probe.setPort(port);

        // Mocked return values
        when(tankService.findAllByIdConfAndControllerPtsConfiguration(1L, configuration)).thenReturn(tank);
        when(probePortRepository.findAllByIdConfiguredAndControllerPtsConfiguration("1", configuration)).thenReturn(Optional.of(port));
        when(probeRepository.findAllByIdConfAndControllerPtsConfiguration(1L, configuration)).thenReturn(null);
        when(probeRepository.save(any(Probe.class))).thenReturn(probe);

        // Act
        Probe savedProbe = probeService.addProbe(probeData, configuration);

        // Assert
        verify(probeRepository, times(1)).save(any(Probe.class));
        assertNotNull(savedProbe);
        assertEquals(probeData.getId(), savedProbe.getIdConf());
        assertEquals(tank, savedProbe.getTank());
        assertEquals(port, savedProbe.getPort());
    }

    @Test
    void mapToProbeConfigDto_ShouldMapProbeToDto() {
        Probe probe = new Probe();
        ProbePort port = new ProbePort();
        probe.setPort(port);
        port.setIdConfigured("134D4769");
        port.setProtocol(1L);
        probe.setAddress(Long.valueOf("1"));
        port.setBaudRate(9600L);

        ProbeConfigDto result = probeService.mapToProbeConfigDto(probe);

        assertNotNull(result);
        assertEquals("134D4769", result.getPortId());
        assertEquals(1L, result.getProtocol());
        assertEquals("1", result.getAddress());
        assertEquals(9600, result.getBaudRate());
    }
}