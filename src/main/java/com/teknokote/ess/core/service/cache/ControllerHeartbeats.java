package com.teknokote.ess.core.service.cache;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class ControllerHeartbeats {
    private final Map<String, LocalDateTime> controllerHeartBeats = new ConcurrentHashMap<>();

    public void updateLastConnection(String ptsId, LocalDateTime connectionTime) {
        controllerHeartBeats.put(ptsId, connectionTime);
    }

    public LocalDateTime getLastConnection(String ptsId) {
        return controllerHeartBeats.get(ptsId);
    }
}
