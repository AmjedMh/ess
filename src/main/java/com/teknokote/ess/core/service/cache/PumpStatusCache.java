package com.teknokote.ess.core.service.cache;

import com.teknokote.ess.core.dao.shifts.ShiftRotationDao;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.service.impl.NozzleService;
import com.teknokote.ess.dto.PeriodDto;
import com.teknokote.ess.dto.data.FuelStatusData;
import com.teknokote.ess.dto.data.PumpStatusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PumpStatusCache {

    @Autowired
    private NozzleService nozzleService;
    @Autowired
    private ShiftRotationDao shiftRotationDao;
    private final ConcurrentHashMap<String, Map<Long, Map<Long, FuelGrade>>> fuelGradeCacheFromUploadStatus = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<Long, List<FuelGrade>>> additionalFuelGradesCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<PumpStatusDto>> stationPumpStatusMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<Long, List<FuelStatusData>>> lastIdleStatusCache = new ConcurrentHashMap<>();
    private final Map<String,Map<Long, PeriodDto>> periodDtoCache = new ConcurrentHashMap<>();


    public Map<String, List<PumpStatusDto>> getStationPumpStatusMap() {
        return stationPumpStatusMap;
    }

    public List<FuelStatusData> updateLastIdleStatusCache(String ptsId, Long pumpId, List<FuelStatusData> fuelStatusDataList) {
        return lastIdleStatusCache.computeIfAbsent(ptsId, k -> new ConcurrentHashMap<>()).put(pumpId, fuelStatusDataList);
    }

    // Utility methods for interacting with the caches
    public FuelGrade updateFuelGradeCache(String ptsId, Long pumpId, Long nozzle, String configurationId) {
        return fuelGradeCacheFromUploadStatus
                .computeIfAbsent(ptsId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(pumpId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(nozzle, nozzles -> nozzleService.findFuelByNozzleAndPump(nozzle, pumpId, configurationId, ptsId));

    }

    public List<FuelGrade> getAdditionalFuelGradesCache(String ptsId, Long pumpId, String configurationId) {
        return additionalFuelGradesCache
                .computeIfAbsent(ptsId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(pumpId, k -> nozzleService.findAllFuelGradesByPump(pumpId, configurationId, ptsId));
    }
    public List<Long> additionalFuelGradesCache(String ptsId, Long pumpId, String configurationId) {

    return additionalFuelGradesCache
            .computeIfAbsent(ptsId, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(pumpId, id ->
            nozzleService.findAllFuelGradesByPump(pumpId, configurationId, ptsId)
            )
            .stream()
                .map(FuelGrade::getIdConf)
                .toList();
    }
    public void updateStationPumpStatusMap(String key, List<PumpStatusDto> value) {
        stationPumpStatusMap.put(key, value);
    }

    public List<FuelStatusData> getLastIdleStatusCache(String ptsId, Long pumpId) {
        return lastIdleStatusCache.computeIfAbsent(ptsId, k -> new ConcurrentHashMap<>()).get(pumpId);
    }
    public void updateCashedPeriodDto(String ptsId, Long pumpId, PeriodDto cashedPeriodDto) {
        periodDtoCache.computeIfAbsent(ptsId, key -> new ConcurrentHashMap<>())
                .put(pumpId, cashedPeriodDto);
    }

    public PeriodDto getCachedPeriodDto(String ptsId, Long pumpId, LocalDateTime currentDateTime) {
        return periodDtoCache
                .computeIfAbsent(ptsId, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(pumpId, id ->
                        shiftRotationDao.dailyPeriodForStation(ptsId, currentDateTime)
                );
    }

}