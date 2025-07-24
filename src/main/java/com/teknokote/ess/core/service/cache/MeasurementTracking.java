package com.teknokote.ess.core.service.cache;

import com.teknokote.pts.client.upload.dto.MeasurementDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
@Getter
@Setter
public class MeasurementTracking {
    // List to store measurements for tracking changes
    private List<MeasurementDto> measurements = new ArrayList<>();
    // Counter to track consecutive increases in product volume
    private int increasingCount = 0;
    // Counter to track consecutive stable or decreasing product volumes
    private int stableOrDecreasingCount = 0;

    public void addInitialMeasurement(MeasurementDto measurementDto) {
        if (measurements.isEmpty()) {
            measurements.add(measurementDto);
        }
    }
    /**
     * Adds a measurement to the list.
     * This method is private because it is internally used by the tracking logic.
     *
     * @param measurementDto the measurement to add
     */
    private void addMeasurement(MeasurementDto measurementDto) {
        measurements.add(measurementDto);
        // Sort by timestamp after every addition
        measurements.sort(Comparator.comparing(MeasurementDto::getDateTime));
    }
    /**
     * Updates the measurement tracking system with a new measurement and compares it
     * with the previous volume to determine trends.
     *
     * @param measurementDto   the new measurement to process
     * @param previousVolume the volume of the previous measurement (can be null for the first entry)
     */
    public void updateMeasurementTracking(MeasurementDto measurementDto, Double previousVolume,Double volumeFluctuation) {
        // Add the new measurement to the list
        addMeasurement(measurementDto);

        // Get the current volume from the measurement
        Double currentVolume = measurementDto.getProductVolume();

        // If there's a previous volume, compare and update counters
        if (previousVolume != null) {
            if (currentVolume > previousVolume && currentVolume - previousVolume > volumeFluctuation) {
                // Indicates a potential tank delivery (volume increased)
                increasingCount++;
                stableOrDecreasingCount = 0; // Reset stable/decreasing counter
            } else if (currentVolume <= previousVolume || currentVolume - previousVolume <= volumeFluctuation) {
                // Indicates stable or decreasing volume
                stableOrDecreasingCount++;
                increasingCount = 0; // Reset increasing counter
            }
        }
    }

    /**
     * clearing measurements and resetting counters.
     * This is typically used after the tank delivery is finalized.
     */
    public void reset() {
        measurements.clear();
        increasingCount = 0;
        stableOrDecreasingCount = 0;
    }
}
