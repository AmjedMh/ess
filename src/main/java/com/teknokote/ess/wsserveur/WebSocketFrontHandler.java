package com.teknokote.ess.wsserveur;

import feign.codec.EncodeException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketFrontHandler extends TextWebSocketHandler {
    private static final ConcurrentHashMap<String, WebSocketSession> sessionsF = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection established: {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received message: {} from session: {}", message.getPayload(), session.getId());

        // Process the received message here
        String payload = message.getPayload();

        // Parse the payload as JSON to retrieve the custom data
        JSONObject customData = new JSONObject(payload);

        // Retrieve the custom attributes or data
        String attribute1 = customData.getString("attribute1");
        sessionsF.put(attribute1, session);

    }

    /**
     * This method is empty intentionally as there is no specific action required
     * when a WebSocket connection is closed. The default behavior is sufficient,
     * and there are no additional clean-up or handling tasks needed in this context.
     *
     * @param session The WebSocket session that has been closed.
     * @param status  The close status containing the reason for the closure.
     * @throws Exception If an error occurs during the handling of the connection closure.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // This method is intentionally empty as no additional action is needed upon WebSocket connection closure.
    }

    public void broadcast(String message, String ptsId) throws EncodeException, IOException {
        synchronized (sessionsF) {

            log.info("this message from the websocket controller :{}",message);
            log.info("this ptsid from the websocket controller :{}",ptsId);
            log.info("seessionF :{}",sessionsF);

            for (Map.Entry<String, WebSocketSession> entry : sessionsF.entrySet()) {

                String sessionId = entry.getKey();

                WebSocketSession session = entry.getValue();
                log.info("this SessionId :{}",sessionId);
                log.info("this condition :{}",sessionId.equals(ptsId));
                if(sessionId.equals(ptsId)){
                    log.info("*********:{}",message);
                    session.sendMessage(new TextMessage(message));
                }
            }
        }
    }
}
