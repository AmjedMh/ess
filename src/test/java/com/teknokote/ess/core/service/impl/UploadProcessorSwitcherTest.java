package com.teknokote.ess.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.controller.upload.pts.info.UploadInformation;
import com.teknokote.ess.controller.upload.pts.processing.base.UploadProcessor;
import com.teknokote.ess.controller.upload.pts.processing.base.UploadProcessorSwitcherImpl;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.pts.client.upload.PTSConfirmationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadProcessorSwitcherTest {

    @Mock
    private ControllerService controllerService;

    @Mock
    private ControllerPtsConfigurationService controllerPtsConfigurationService;

    @Mock
    private FirmwareInformationService firmwareInformationService;

    @Mock
    private MessagePTSLogService messagePTSLogService;

    @Mock
    private UploadProcessor uploadProcessor;

    @InjectMocks
    private UploadProcessorSwitcherImpl uploadProcessorSwitcher;


    @Mock
    private UploadInformation mockUploadInformation;
    @Mock
    private ControllerPts controllerPtsDto;
    private String ptsId;
    private String configurationId;
    private String firmwareVersion;
    @Mock
    private UploadInformation.UploadPacketInformation uploadPacketInformation;
    @Mock
    private UploadInformation.UploadPacketInformation.Data data;

    @Mock
    private ControllerPtsConfiguration controllerPtsConfiguration;
    private TextMessage messagePayload(Path filePath) throws IOException {
        return new TextMessage(Files.readString(filePath));

    }
    @BeforeEach
    void setUp() throws Exception {
        ptsId = "0027003A3438510935383135";
        firmwareVersion = "25-01-06T23:59:14";
        configurationId = "699c8c2";
        ObjectMapper objectMapper = new ObjectMapper();

        // Load JSON file
        Path filePath = Paths.get("src/test/resources/uploads/upload-packet-information.json");
        String jsonContent = Files.readString(filePath);
        controllerPtsDto.setId(1L);
        controllerPtsDto.setPtsId(ptsId);

        controllerPtsConfiguration.setConfigurationId(configurationId);
        controllerPtsConfiguration.setPtsId(ptsId);
        // Deserialize into UploadPacketInformation
        uploadPacketInformation = objectMapper.readValue(jsonContent, UploadInformation.UploadPacketInformation.class);

        mockUploadInformation.setIdentifiedVersion(firmwareVersion);
        mockUploadInformation.setPtsId(ptsId);
        mockUploadInformation.setIdentifiedConfigurationId(configurationId);
        mockUploadInformation.setIdentifiedControllerPts(controllerPtsDto);
        mockUploadInformation.setIdentifiedControllerPtsConfiguration(controllerPtsConfiguration);
        mockUploadInformation.setUploadPacketInformations(List.of(uploadPacketInformation));
    }

    @Test
    void testProcess_Successful() throws IOException {
        when(controllerPtsConfigurationService.configurationExists(anyString(), anyString())).thenReturn(true);
        when(controllerService.findController(ptsId)).thenReturn(controllerPtsDto);
        when(firmwareInformationService.firmwareVersion(ptsId)).thenReturn(firmwareVersion);
        when(controllerPtsConfigurationService.findByPtsIdAndConfigurationId(ptsId, configurationId)).thenReturn(controllerPtsConfiguration);

        Path filePath = Paths.get("src/test/resources/uploads/upload-pump-transaction.json");
        TextMessage textMessage = messagePayload(filePath);

        PTSConfirmationResponse response = uploadProcessorSwitcher.process(textMessage.getPayload(), 1L, true);

        assertNotNull(response);
        assertEquals(ptsId, response.getPtsId());
    }

    @Test
    void testProcess_UnsupportedFirmware() throws IOException {
        when(controllerPtsConfigurationService.configurationExists(anyString(), anyString())).thenReturn(true);
        when(controllerService.findController(ptsId)).thenReturn(controllerPtsDto);
        when(firmwareInformationService.firmwareVersion(ptsId)).thenReturn(firmwareVersion);
        when(controllerPtsConfigurationService.findByPtsIdAndConfigurationId(ptsId, configurationId)).thenReturn(controllerPtsConfiguration);

        Path filePath = Paths.get("src/test/resources/uploads/upload-pump-transaction.json");
        TextMessage textMessage = messagePayload(filePath);
        PTSConfirmationResponse response = uploadProcessorSwitcher.process(textMessage.getPayload(), 1L, false);

        assertNotNull(response);
        assertEquals("Firmware not supported", response.getPackets().get(0).getMessage());
    }
}

