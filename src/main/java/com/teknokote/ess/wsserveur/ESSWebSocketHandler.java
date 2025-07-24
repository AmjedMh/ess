package com.teknokote.ess.wsserveur;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.controller.upload.pts.processing.UploadProcessorSwitcher;
import com.teknokote.ess.core.service.cache.ControllerHeartbeats;
import com.teknokote.ess.core.service.impl.FirmwareInformationService;
import com.teknokote.ess.core.service.impl.MessagePTSLogService;
import com.teknokote.ess.dto.UploadedRecordsInformationDto;
import com.teknokote.ess.events.listeners.WebSocketMessageEvent;
import com.teknokote.ess.exceptions.WebSocketNotConnectedException;
import com.teknokote.ess.utils.EssUtils;
import com.teknokote.pts.client.request.JsonPTSQuery;
import com.teknokote.pts.client.request.RequestPacket;
import com.teknokote.pts.client.response.configuration.PTSDateTime;
import com.teknokote.pts.client.upload.PTSConfirmationResponse;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.exceptions.WebSocketException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ESSWebSocketHandler extends TextWebSocketHandler {
    @Autowired
    private FirmwareInformationService firmwareInformationService;
    @Autowired
    private UploadProcessorSwitcher uploadProessorSwitcher;
    @Autowired
    private MessagePTSLogService messagePtsLogService;
    @Value("#{'${ess.firmware.supported-version}'.split(',')}")
    private List<String> firmwareSupportedVersions;
    @Value("${app.logging.uploads}")
    private Boolean logPackets;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private ControllerHeartbeats controllerHeartbeats;
    public final ConcurrentHashMap<String, ESSWebSocketSession> sessions = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, PTSDateTime> dateTimeResponses = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, UploadedRecordsInformationDto> uploadedInformationResponses = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String xPtsId = EssUtils.getKeyFromWebSocketSession(session, EssUtils.EnumHeaderAttributes.PTS_ID);
        String firmwareVersion = EssUtils.getKeyFromWebSocketSession(session, EssUtils.EnumHeaderAttributes.FIRMWARE_VERSION_DATE_TIME);
        String configurationId = EssUtils.getKeyFromWebSocketSession(session, EssUtils.EnumHeaderAttributes.CONFIGURATION_ID);

        log.info("[afterConnectionEstablished] session id:{}, pts-version-configId: {}, {}, {} ", session.getId(), xPtsId, firmwareVersion, configurationId);

        if (StringUtils.hasText(xPtsId)) {
            final ESSWebSocketSession essWebSocketSession = ESSWebSocketSession.builder()
                    .session(session)
                    .ptsId(xPtsId)
                    .configurationId(configurationId)
                    .firmwareVersion(StringUtils.hasText(firmwareVersion) ? firmwareVersion : "-")
                    .firmwareVersionSupported(firmwareSupportedVersions.contains(firmwareVersion))
                    .build();
            sessions.put(xPtsId, essWebSocketSession);
            controllerHeartbeats.updateLastConnection(xPtsId, LocalDateTime.now());
            firmwareInformationService.updateFirmwareInformation(essWebSocketSession.getPtsId(), essWebSocketSession.getFirmwareVersion(), essWebSocketSession.isFirmwareVersionSupported());
        } else {
            log.info("Not a PTS-2 controller!");
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        final String ptsId = EssUtils.getKeyFromWebSocketSession(session, EssUtils.EnumHeaderAttributes.PTS_ID);
        log.info("Controlle : {},essWebSocketSession :{}", ptsId,Objects.isNull(ptsId)?"null pts":ptsId);
        if(Objects.nonNull(logPackets) && Boolean.TRUE.equals(logPackets)) {
            log.info("Received packet:{}",message.getPayload());
        }
        ESSWebSocketSession essWebSocketSession = sessions.get(ptsId);
        if (essWebSocketSession == null) {
            // Il arrive des fois que handleTextMessage est appelé avant que afterConnectionEstablished soit déclenchée
            log.info("handleTextMessage called with unknown pts id:{}. Recovering.", ptsId);
            afterConnectionEstablished(session);
            essWebSocketSession = sessions.get(ptsId);
        }
        // Sauvegarder le retour du controlleur en extractant le PtsId
        Long traceId = messagePtsLogService.logReceivedWSSMsg(essWebSocketSession.getPtsId(), essWebSocketSession.getFirmwareVersion(), message.getPayload());
        final String messagePayload = message.getPayload();
        if (messagePayload.contains("\"Type\":\"DateTime\"")) {
            // Parse the DateTime response from the messagePayload
            PTSDateTime responsePacket = parseDateTimeResponse(messagePayload);
            dateTimeResponses.put(ptsId, responsePacket);
        }
        if (messagePayload.contains("PumpAuthorizeConfirmation")) {
            eventPublisher.publishEvent(new WebSocketMessageEvent(this, messagePayload));
        }

        if (messagePayload.contains("\"Type\":\"UploadedRecordsInformation\"")) {
            UploadedRecordsInformationDto responsePacket = uploadedInformationResponse(messagePayload);
            uploadedInformationResponses.put(ptsId, responsePacket);
        }

        if (!messagePayload.contains("Upload")) {
            // Type de message non traité ==> juste le logger
            log.info("Received message from controller:{}", messagePayload);
            return;
        }
        if (messagePayload.contains("\"Type\":\"UploadPumpTransaction\"")) {
            eventPublisher.publishEvent(new WebSocketMessageEvent(this, messagePayload));
            log.info("Received message from controller:{}", messagePayload);
        }
        final PTSConfirmationResponse response = uploadProessorSwitcher.process(messagePayload, traceId,essWebSocketSession.isFirmwareVersionSupported());
        final String jsonResponse = new ObjectMapper().writeValueAsString(response);
        sendMessageToController(essWebSocketSession, jsonResponse);
    }

    private PTSDateTime parseDateTimeResponse(String messagePayload) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(messagePayload);
        JsonNode data = root.path("Packets").get(0).path("Data");

        return PTSDateTime.builder()
                .dateTime(data.path("DateTime").asText())
                .autoSynchronize(data.path("AutoSynchronize").asBoolean())
                .uTCOffset(data.path("UTCOffset").asLong())
                .build();
    }

    public UploadedRecordsInformationDto uploadedInformationResponse(String messagePayload) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(messagePayload);
        JsonNode data = root.path("Packets").get(0).path("Data");

        return UploadedRecordsInformationDto.builder()
                .pumpTransactionsUploaded(data.path("PumpTransactionsUploaded").longValue())
                .pumpTransactionsTotal(data.path("PumpTransactionsTotal").longValue())
                .tankMeasurementsUploaded(data.path("TankMeasurementsUploaded").longValue())
                .tankMeasurementsTotal(data.path("TankMeasurementsTotal").longValue())
                .inTankDeliveriesUploaded(data.path("InTankDeliveriesUploaded").longValue())
                .inTankDeliveriesTotal(data.path("InTankDeliveriesTotal").longValue())
                .gpsRecordsUploaded(data.path("GpsRecordsUploaded").longValue())
                .gpsRecordsTotal(data.path("GpsRecordsTotal").longValue())
                .alertRecordsUploaded(data.path("AlertRecordsUploaded").longValue())
                .alertRecordsTotal(data.path("AlertRecordsTotal").longValue())
                .build();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().removeIf(s -> Objects.equals(s.getSession().getId(), session.getId()));
        log.info("[afterConnectionClosed] session id:{}, Sessions size: {} ", session.getId(), sessions.size());
    }


    /**
     * Sends requestPaquets to the controller identified by "ptsId"
     *
     * @param ptsId
     * @param requestPacket
     */
    public void sendRequests(String ptsId, List<RequestPacket> requestPacket) {
        final ESSWebSocketSession webSocketSession = sessions.get(ptsId);
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            throw new WebSocketNotConnectedException("WebSocket session is not available or not open for ptsId: " + ptsId);
        }
        JsonPTSQuery query = JsonPTSQuery.builder().packets(requestPacket).protocol("jsonPTS").ptsId(ptsId).build();
        sendQueryToController(webSocketSession, query);
    }

    /**
     * Sends query of type JsonPTSQuery to the controller
     *
     * @param essWebSocketSession
     * @param query
     */
    public void sendQueryToController(ESSWebSocketSession essWebSocketSession, JsonPTSQuery query) {
        String message = "";
        try {
            message = (new ObjectMapper()).writeValueAsString(query);
            essWebSocketSession.sendMessage(message);
        } catch (IOException e) {
            messagePtsLogService.logWSSMsgFail(essWebSocketSession.getPtsId(), essWebSocketSession.getFirmwareVersion(), message);
            throw new WebSocketException(e);
        }
        messagePtsLogService.logWSSMsgSuccess(essWebSocketSession.getPtsId(), essWebSocketSession.getFirmwareVersion(), message);
    }

    /**
     * Permet d'envoyer un message au contrôleur.
     * La méthode permet de tracer le message envoyé et sa nature
     */
    private void sendMessageToController(ESSWebSocketSession essWebSocketSession, String message) {
        try {
            essWebSocketSession.sendMessage(message);
        } catch (IOException e) {
            messagePtsLogService.logWSSMsgFail(essWebSocketSession.getPtsId(), essWebSocketSession.getFirmwareVersion(), message);
            throw new WebSocketException(e);
        }
        messagePtsLogService.logWSSMsgSuccess(essWebSocketSession.getPtsId(), essWebSocketSession.getFirmwareVersion(), message);
    }

    public PTSDateTime getDateTimeFromController(String ptsId, List<RequestPacket> requestPacket) {
        sendRequests(ptsId, requestPacket);
        return dateTimeResponses.get(ptsId);
    }

    public UploadedRecordsInformationDto getUploadedInformationFromController(String ptsId, List<RequestPacket> requestPacket) {
        sendRequests(ptsId, requestPacket);
        return uploadedInformationResponses.get(ptsId);
    }
}
