package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.wsserveur.WebSocketFrontHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebSocketFrontHandlerTest {
    private WebSocketFrontHandler webSocketFrontHandler;
    private WebSocketSession mockSession;

    @BeforeEach
    void setUp() {
        webSocketFrontHandler = new WebSocketFrontHandler();
        mockSession = Mockito.mock(WebSocketSession.class);
    }

    @Test
    void afterConnectionEstablished_ShouldLogConnectionEstablished() throws Exception {
        // Simulate the connection established
        webSocketFrontHandler.afterConnectionEstablished(mockSession);
        verify(mockSession, times(1)).getId();
    }

    @Test
    void handleTextMessage_ShouldAddSessionToMap() throws Exception {
        String jsonMessage = "{\"attribute1\": \"0027003A3438510935383135\"}"; // Updated key
        TextMessage textMessage = new TextMessage(jsonMessage);

        when(mockSession.getId()).thenReturn("sessionId");

        webSocketFrontHandler.handleTextMessage(mockSession, textMessage);

        // Check if the session is added to the map with the correct attribute
        ConcurrentHashMap<String, WebSocketSession> sessionsMap = getSessionsMap();

        assertEquals(1, sessionsMap.size());
        assertTrue(sessionsMap.containsKey("0027003A3438510935383135"));
        assertEquals(mockSession, sessionsMap.get("0027003A3438510935383135"));
    }

    @Test
    void afterConnectionClosed_ShouldNotThrowException() {
        assertDoesNotThrow(() -> webSocketFrontHandler.afterConnectionClosed(mockSession, CloseStatus.NORMAL));
    }

    @Test
    void broadcast_ShouldSendMessageToCorrectSession() throws Exception {
        String ptsId = "0027003A3438510935383135";

        // Create a JSON object for the message
        String message = "{\"content\": \"Hello WebSocket\"}";

        // Mock the expected behavior of the session
        when(mockSession.getId()).thenReturn("sessionId");

        // Simulate adding the session to the map first
        webSocketFrontHandler.handleTextMessage(mockSession, new TextMessage("{\"attribute1\": \"" + ptsId + "\"}"));

        // Now broadcast the message
        webSocketFrontHandler.broadcast(message, ptsId);

        // Verify that the sendMessage method is called with TextMessage containing valid JSON
        verify(mockSession, times(1)).sendMessage(argThat(textMessage -> {
            try {
                // Correctly parse the payload as JSON
                JSONObject obj = new JSONObject(String.valueOf(textMessage.getPayload()));
                return obj.getString("content").equals("Hello WebSocket");
            } catch (JSONException e) {
                return false; // Not a valid JSON
            }
        }));
    }

    @Test
    void broadcast_ShouldNotSendMessageIfSessionNotFound() throws Exception {
        String ptsId = "nonExistingPtsId"; // Updated to ensure we use a different key
        String message = "{\"content\":\"Hello WebSocket\"}"; // Ensure this is a JSON formatted string

        // Broadcast message without any sessions stored
        webSocketFrontHandler.broadcast(message, ptsId);

        // There should be no interaction with mockSession since it wasn't added
        verify(mockSession, never()).sendMessage(any(TextMessage.class));
    }

    private ConcurrentHashMap<String, WebSocketSession> getSessionsMap() {
        // Access the private static sessionsF map directly using reflection
        try {
            java.lang.reflect.Field field = WebSocketFrontHandler.class.getDeclaredField("sessionsF");
            field.setAccessible(true);
            return (ConcurrentHashMap<String, WebSocketSession>) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}