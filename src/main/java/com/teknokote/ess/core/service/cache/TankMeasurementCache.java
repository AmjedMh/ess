package com.teknokote.ess.core.service.cache;

import com.teknokote.pts.client.upload.dto.MeasurementDto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TankMeasurementCache {

   private final Map<String, Map<Long, MeasurementDto>> realTimeCache = new HashMap<>();
   private final Map<String, Map<Long, MeasurementDto>> delayedCache = new HashMap<>();

   /**
    * Retrieves the last real-time measurement data.
    * If not found in cache, loads it from DB using the provided function.
    */
   public MeasurementDto getLastMeasurementData(String controllerPtsId, Long tankId, Function<? super Long, ? extends MeasurementDto> loadFromDBFn) {
      realTimeCache.putIfAbsent(controllerPtsId, new HashMap<>());
      Map<Long, MeasurementDto> tankMap = realTimeCache.get(controllerPtsId);
      return tankMap.computeIfAbsent(tankId, loadFromDBFn);
   }

   /**
    * Updates the real-time measurement cache with new data.
    */
   public void updateMeasurementCache(String controllerPtsId, MeasurementDto measurementDto) {
      if (measurementDto != null) {
         realTimeCache
                 .computeIfAbsent(controllerPtsId, k -> new HashMap<>())
                 .put(measurementDto.getTank(), measurementDto);
      }
   }

   /**
    * Retrieves all cached real-time measurements for a PTS ID.
    */
   public Map<Long, MeasurementDto> getTankMeasurementCache(String ptsId) {
      return realTimeCache.getOrDefault(ptsId, Collections.emptyMap());
   }

   // ================================
   // NEW METHODS FOR DELAYED CACHE
   // ================================

   /**
    * Retrieves the last delayed measurement from cache (if any).
    */
   /**
    * Retrieves the last delayed measurement data.
    * If not found in cache, loads it from DB using the provided function.
    */
   public MeasurementDto getLastDelayedMeasurementData(String ptsId, Long tankId, Function<? super Long, ? extends MeasurementDto> loadFromDBFn) {
      delayedCache.putIfAbsent(ptsId, new HashMap<>());
      Map<Long, MeasurementDto> tankMap = delayedCache.get(ptsId);
      return tankMap.computeIfAbsent(tankId, loadFromDBFn);
   }


   /**
    * Updates the delayed measurement cache with new data.
    */
   public void updateDelayedMeasurementCache(String ptsId, MeasurementDto measurementDto) {
      if (measurementDto != null) {
         delayedCache
                 .computeIfAbsent(ptsId, k -> new HashMap<>())
                 .put(measurementDto.getTank(), measurementDto);
      }
   }

   /**
    * Optional: Get full delayed cache for inspection or debugging.
    */
   public Map<Long, MeasurementDto> getDelayedTankMeasurementCache(String ptsId) {
      return delayedCache.getOrDefault(ptsId, Collections.emptyMap());
   }
}
