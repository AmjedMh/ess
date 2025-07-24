package com.teknokote.ess.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.controller.upload.pts.processing.UploadProcessorSwitcher;
import com.teknokote.ess.core.service.cache.ControllerHeartbeats;
import com.teknokote.ess.events.listeners.WebSocketMessageEvent;
import com.teknokote.ess.exceptions.WebSocketNotConnectedException;
import com.teknokote.ess.wsserveur.ESSWebSocketHandler;
import com.teknokote.ess.wsserveur.ESSWebSocketSession;
import com.teknokote.pts.client.request.RequestPacket;
import com.teknokote.pts.client.upload.PTSConfirmationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ESSWebSocketHandlerTest {

    @InjectMocks
    private ESSWebSocketHandler essWebSocketHandler;
    @Mock
    private FirmwareInformationService firmwareInformationService;
    @Mock
    private MessagePTSLogService messagePtsLogService;
    @Mock
    private UploadProcessorSwitcher uploadProcessorSwitcher;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ControllerHeartbeats controllerHeartbeats;
    @Mock
    private ESSWebSocketSession essWebSocketSession;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private WebSocketSession session;
    private static String ptsId;
    private static String firmwareVersion;
    private static String configurationId;
    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        ptsId = "0027003A3438510935383135";
        firmwareVersion = "25-01-06T23:59:14";
        configurationId = "150e9c08";
        HttpHeaders handshakeHeaders = new HttpHeaders();
        // Setup handshake headers with PTS_ID, firmware version, and configuration ID
        handshakeHeaders.add("X-Pts-Id", ptsId);
        handshakeHeaders.add("X-Pts-Firmware-Version-DateTime", firmwareVersion);
        handshakeHeaders.add("X-Pts-Configuration-Identifier", configurationId);
        lenient().when(session.getHandshakeHeaders()).thenReturn(handshakeHeaders);
        essWebSocketSession = ESSWebSocketSession.builder()
                .session(session)
                .ptsId(ptsId)
                .configurationId(configurationId)
                .firmwareVersion(firmwareVersion)
                .firmwareVersionSupported(true)
                .build();
        setPrivateField(essWebSocketHandler, "firmwareSupportedVersions", List.of("25-01-06T23:59:14", "25-02-15T12:30:00"));
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    private TextMessage messagePayload(Path filePath) throws IOException {
        return new TextMessage(Files.readString(filePath));

    }
    @Test
    void testAfterConnectionEstablished_ValidPTSId(){
        // Act
        essWebSocketHandler.afterConnectionEstablished(session);

        // Assert that the correct methods on the mocks were called
        verify(controllerHeartbeats).updateLastConnection(eq(ptsId), any());
        verify(firmwareInformationService).updateFirmwareInformation(eq(ptsId), eq(firmwareVersion), eq(true));

        // Verify the session was added
        assertEquals(1, essWebSocketHandler.sessions.size());
        assertEquals(essWebSocketHandler.sessions.get(ptsId).getPtsId(), ptsId);
    }

    @Test
    void testAfterConnectionEstablished_MissingPTSId(){

        HttpHeaders handshakeHeaders = new HttpHeaders();
        // Add only firmware version and configuration ID, but NO PTS_ID
        handshakeHeaders.add("X-Pts-Firmware-Version-DateTime", firmwareVersion);
        handshakeHeaders.add("X-Pts-Configuration-Identifier", configurationId);

        when(session.getHandshakeHeaders()).thenReturn(handshakeHeaders);

        // Act
        essWebSocketHandler.afterConnectionEstablished(session);

        // Assert that session was NOT added
        assertTrue(essWebSocketHandler.sessions.isEmpty(), "Session map should remain empty");

        // Verify that updateLastConnection and updateFirmwareInformation were NOT called
        verify(controllerHeartbeats, never()).updateLastConnection(anyString(), any());
        verify(firmwareInformationService, never()).updateFirmwareInformation(anyString(), anyString(), anyBoolean());
    }

    @Test
    void testHandleTextMessage_DateTimeResponse() throws Exception {
        Path filePath = Paths.get("src/test/resources/uploads/pts-date-time.json");
        TextMessage textMessage = messagePayload(filePath);
        when(messagePtsLogService.logReceivedWSSMsg(any(), any(), any())).thenReturn(1L);
        essWebSocketHandler.sessions.put(ptsId, essWebSocketSession);

        // Act
        essWebSocketHandler.handleTextMessage(session, textMessage);

        // Assert
        assertTrue(essWebSocketHandler.dateTimeResponses.containsKey(ptsId), "DateTime response should be stored");
    }

    @Test
    void testHandleTextMessage_PumpAuthorizeConfirmation() throws Exception {
        Path filePath = Paths.get("src/test/resources/uploads/pump-authorize-confirmation.json");
        TextMessage textMessage = messagePayload(filePath);

        essWebSocketHandler.sessions.put(ptsId, essWebSocketSession);
        when(messagePtsLogService.logReceivedWSSMsg(any(), any(), any())).thenReturn(1L);
        // Act
        essWebSocketHandler.handleTextMessage(session, textMessage);
        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(WebSocketMessageEvent.class));
    }

    @Test
    void testHandleTextMessage_UploadedRecordsInformation() throws Exception {
        Path filePath = Paths.get("src/test/resources/uploads/upload-records-information.json");
        TextMessage textMessage = messagePayload(filePath);

        when(messagePtsLogService.logReceivedWSSMsg(any(), any(), any())).thenReturn(1L);
        essWebSocketHandler.sessions.put(ptsId, essWebSocketSession);

        // Act
        essWebSocketHandler.handleTextMessage(session, textMessage);

        // Assert
        assertEquals(15L, essWebSocketHandler.uploadedInformationResponse(textMessage.getPayload()).getPumpTransactionsTotal());
        assertTrue(essWebSocketHandler.uploadedInformationResponses.containsKey(ptsId), "UploadedRecordsInformation should be stored");
    }

    @Test
    void testHandleTextMessage_UploadPumpTransaction() throws Exception {
        Path filePath = Paths.get("src/test/resources/uploads/upload-pump-transaction.json");
        TextMessage textMessage = messagePayload(filePath);
        PTSConfirmationResponse mockResponse = new PTSConfirmationResponse();

        when(messagePtsLogService.logReceivedWSSMsg(any(), any(), any())).thenReturn(1L);
        when(uploadProcessorSwitcher.process(eq(textMessage.getPayload()), anyLong(), anyBoolean())).thenReturn(mockResponse);
        essWebSocketHandler.sessions.put(ptsId, essWebSocketSession);

        // Act
        essWebSocketHandler.handleTextMessage(session, textMessage);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(WebSocketMessageEvent.class));
        verify(uploadProcessorSwitcher, times(1)).process(eq(textMessage.getPayload()), anyLong(), anyBoolean());
    }

    @Test
    void testSendRequests_SessionOpen(){
        RequestPacket requestPacket = new RequestPacket();
        essWebSocketHandler.sessions.put(ptsId, essWebSocketSession);
        when(essWebSocketSession.isOpen()).thenReturn(true);

        essWebSocketHandler.sendRequests(ptsId, Arrays.asList(requestPacket));

        // Verify interactions
        verify(messagePtsLogService).logWSSMsgSuccess(eq(ptsId), anyString(), anyString());
    }

    @Test
    void testSendRequests_SessionNotOpenNotAvailable() {
        RequestPacket requestPacket = new RequestPacket();

        // Use a helper method to perform the action
        assertThrows(WebSocketNotConnectedException.class, () -> sendRequestAndHandleException(ptsId, Arrays.asList(requestPacket)));
    }

    // Helper method to send the request
    private void sendRequestAndHandleException(String ptsId, List<RequestPacket> requestPackets) {
        essWebSocketHandler.sendRequests(ptsId, requestPackets);
    }
}