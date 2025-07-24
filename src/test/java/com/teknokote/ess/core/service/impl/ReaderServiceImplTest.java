package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.ProbePort;
import com.teknokote.ess.core.model.configuration.Reader;
import com.teknokote.ess.core.repository.ProbePortRepository;
import com.teknokote.ess.core.repository.ReaderRepository;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSProbePort;
import com.teknokote.pts.client.response.configuration.PTSReadersConfiguration;
import com.teknokote.pts.client.response.configuration.PTSReadersConfigurationResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSReadersConfigurationResponsePacketData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReaderServiceImplTest {

    @InjectMocks
    private ReaderServiceImpl readerService;
    @Mock
    private ReaderRepository readerRepository;
    @Mock
    private ProbePortRepository probePortRepository;
    @Mock
    private ControllerPtsConfigurationService controllerPtsConfigurationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addNewReader_ShouldAddReadersAndPorts() {
        // Arrange
        ControllerPtsConfiguration controllerPtsConfiguration = mock(ControllerPtsConfiguration.class);
        JsonPTSResponse response = mock(JsonPTSResponse.class);
        PTSReadersConfigurationResponsePacket packet = mock(PTSReadersConfigurationResponsePacket.class);
        PTSReadersConfigurationResponsePacketData data = mock(PTSReadersConfigurationResponsePacketData.class);
        PTSProbePort port = mock(PTSProbePort.class);
        PTSReadersConfiguration readerConfig = mock(PTSReadersConfiguration.class);

        when(response.getPackets()).thenReturn(Collections.singletonList(packet));
        when(packet.getData()).thenReturn(data);
        when(data.getPorts()).thenReturn(Collections.singletonList(port));
        when(data.getReaders()).thenReturn(Collections.singletonList(readerConfig));
        when(readerConfig.getId()).thenReturn(1L);
        when(readerConfig.getPort()).thenReturn("1");

        ProbePort probePort = new ProbePort();
        probePort.setIdConfigured("1");
        when(probePortRepository.findAllByIdConfiguredAndControllerPtsConfiguration("1", controllerPtsConfiguration))
                .thenReturn(Optional.of(probePort));

        // Act
        JsonPTSResponse result = readerService.addNewReader(controllerPtsConfiguration, response);

        // Assert
        assertSame(result, response);
        verify(probePortRepository, times(1)).findAllByIdConfiguredAndControllerPtsConfiguration("1", controllerPtsConfiguration);
        verify(readerRepository, times(1)).save(any(Reader.class));
    }

    @Test
    void addReader_ShouldSaveReader() {
        // Arrange
        ControllerPtsConfiguration controllerPtsConfiguration = mock(ControllerPtsConfiguration.class);

        PTSReadersConfiguration readerConfig = new PTSReadersConfiguration();
        readerConfig.setId(1L);
        readerConfig.setPort("1");
        readerConfig.setAddress(1L);
        readerConfig.setPumpId(1L);

        ProbePort port = new ProbePort();
        port.setIdConfigured("1");

        Reader reader = new Reader();
        reader.setId(1L);
        reader.setIdConf(1L);
        reader.setPort(port);
        reader.setPumpId(1L);
        reader.setAddress(1L);

        when(probePortRepository.findAllByIdConfiguredAndControllerPtsConfiguration("1", controllerPtsConfiguration)).thenReturn(Optional.of(port));
        when(readerRepository.save(any(Reader.class))).thenReturn(reader);

        Reader savedReader = readerService.addReader(controllerPtsConfiguration, readerConfig);

        // Assert
        assertEquals(1L, savedReader.getIdConf());
        assertEquals(1L, savedReader.getAddress());
        assertEquals(1L, savedReader.getPumpId());
        assertEquals(port, savedReader.getPort());
        verify(readerRepository, times(1)).save(any(Reader.class));
    }

    @Test
    void addPort_ShouldCreateOrUpdateProbePort() {
        // Arrange
        ControllerPtsConfiguration controllerPtsConfiguration = mock(ControllerPtsConfiguration.class);
        PTSProbePort portPb = mock(PTSProbePort.class);

        when(portPb.getId()).thenReturn("1");
        when(portPb.getProtocol()).thenReturn(1L);
        when(portPb.getBaudRate()).thenReturn(9600L);

        when(probePortRepository.findAllByIdConfiguredAndControllerPtsConfiguration("1", controllerPtsConfiguration))
                .thenReturn(Optional.empty()); // Simulate new port creation

        // Act
        readerService.addPort(portPb, controllerPtsConfiguration);

        // Assert
        ArgumentCaptor<ProbePort> captor = ArgumentCaptor.forClass(ProbePort.class);
        verify(probePortRepository, times(1)).save(captor.capture());
        ProbePort savedPort = captor.getValue();

        assertNotNull(savedPort);
        assertEquals("1", savedPort.getIdConfigured());
        assertEquals(1, savedPort.getProtocol());
        assertEquals(9600, savedPort.getBaudRate());
        assertEquals(controllerPtsConfiguration, savedPort.getControllerPtsConfiguration());
    }
}