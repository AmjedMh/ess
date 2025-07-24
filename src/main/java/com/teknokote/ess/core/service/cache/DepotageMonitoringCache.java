package com.teknokote.ess.core.service.cache;

import com.teknokote.pts.client.upload.dto.UploadSource;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class DepotageMonitoringCache {
    private final Map<String, Map<Long, Map<UploadSource, MeasurementTracking>>> depotageMonitoring = new HashMap<>();

    public MeasurementTracking getOrCreateMeasurementTracking(String ptsId, Long tankId, UploadSource type) {
        depotageMonitoring.computeIfAbsent(ptsId, k -> new HashMap<>());
        Map<Long, Map<UploadSource, MeasurementTracking>> tankMap = depotageMonitoring.get(ptsId);
        tankMap.computeIfAbsent(tankId, k -> new EnumMap<>(UploadSource.class));
        Map<UploadSource, MeasurementTracking> trackingMap = tankMap.get(tankId);
        return trackingMap.computeIfAbsent(type, k -> new MeasurementTracking());
    }
}

