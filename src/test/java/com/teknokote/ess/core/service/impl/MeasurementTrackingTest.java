package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.service.cache.MeasurementTracking;
import com.teknokote.pts.client.upload.dto.MeasurementDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeasurementTrackingTest {

    private static final Logger log = LoggerFactory.getLogger(MeasurementTrackingTest.class);
    private MeasurementTracking tracking;

    @BeforeEach
    void setUp() {
        tracking = new MeasurementTracking();
    }

    @Test
    void testAddInitialMeasurement() {
        log.info("Testing addInitialMeasurement with a single measurement.");
        MeasurementDto measurement = new MeasurementDto();
        measurement.setProductVolume(100.0);

        tracking.addInitialMeasurement(measurement);

        List<MeasurementDto> measurements = tracking.getMeasurements();
        assertEquals(1, measurements.size());
        assertEquals(100.0, measurements.get(0).getProductVolume());
        log.info("addInitialMeasurement test passed. initial Measurement volume :{}",measurements.get(0).getProductVolume());
    }

    @Test
    void testAddInitialMeasurementDoesNotAddTwice() {
        log.info("Testing addInitialMeasurement does not add multiple times.");
        MeasurementDto measurement1 = new MeasurementDto();
        measurement1.setProductVolume(100.0);

        MeasurementDto measurement2 = new MeasurementDto();
        measurement2.setProductVolume(200.0);

        tracking.addInitialMeasurement(measurement1);
        tracking.addInitialMeasurement(measurement2);

        List<MeasurementDto> measurements = tracking.getMeasurements();
        assertEquals(1, measurements.size());
        assertEquals(100.0, measurements.get(0).getProductVolume());
        log.info("addInitialMeasurement does not add multiple times test passed.initial Measurement volume :{}",measurements.get(0).getProductVolume());
    }

    @Test
    void testUpdateMeasurementTracking_IncreasingVolume() {
        log.info("Testing updateMeasurementTracking with increasing volume.");
        MeasurementDto measurement1 = new MeasurementDto();
        measurement1.setProductVolume(100.0);
        measurement1.setDateTime(LocalDateTime.now());
        MeasurementDto measurement2 = new MeasurementDto();
        measurement2.setProductVolume(120.0);
        measurement2.setDateTime(LocalDateTime.now().minusMinutes(1));
        tracking.addInitialMeasurement(measurement1);
        tracking.updateMeasurementTracking(measurement2, 100.0, 5.0);

        assertEquals(2, tracking.getMeasurements().size());
        assertEquals(1, tracking.getIncreasingCount());
        assertEquals(0, tracking.getStableOrDecreasingCount());
        log.info("updateMeasurementTracking with increasing volume test passed.");
    }

    @Test
    void testUpdateMeasurementTracking_StableVolume() {
        log.info("Testing updateMeasurementTracking with stable volume.");
        MeasurementDto measurement1 = new MeasurementDto();
        measurement1.setProductVolume(100.0);
        measurement1.setDateTime(LocalDateTime.now());

        MeasurementDto measurement2 = new MeasurementDto();
        measurement2.setProductVolume(102.0);
        measurement2.setDateTime(LocalDateTime.now().minusMinutes(1));

        tracking.addInitialMeasurement(measurement1);
        tracking.updateMeasurementTracking(measurement2, 100.0, 5.0);

        assertEquals(2, tracking.getMeasurements().size());
        assertEquals(0, tracking.getIncreasingCount());
        assertEquals(1, tracking.getStableOrDecreasingCount());
        log.info("updateMeasurementTracking with stable volume test passed.");
    }

    @Test
    void testUpdateMeasurementTracking_DecreasingVolume() {
        log.info("Testing updateMeasurementTracking with decreasing volume.");
        MeasurementDto measurement1 = new MeasurementDto();
        measurement1.setProductVolume(100.0);
        measurement1.setDateTime(LocalDateTime.now());
        MeasurementDto measurement2 = new MeasurementDto();
        measurement2.setProductVolume(80.0);
        measurement2.setDateTime(LocalDateTime.now().minusMinutes(1));

        tracking.addInitialMeasurement(measurement1);
        tracking.updateMeasurementTracking(measurement2, 100.0, 5.0);

        assertEquals(2, tracking.getMeasurements().size());
        assertEquals(0, tracking.getIncreasingCount());
        assertEquals(1, tracking.getStableOrDecreasingCount());
        log.info("updateMeasurementTracking with decreasing volume test passed.");
    }

    @Test
    void testReset() {
        log.info("Testing reset functionality.");
        MeasurementDto measurement1 = new MeasurementDto();
        measurement1.setProductVolume(100.0);
        measurement1.setDateTime(LocalDateTime.now());
        MeasurementDto measurement2 = new MeasurementDto();
        measurement2.setProductVolume(120.0);
        measurement2.setDateTime(LocalDateTime.now().minusMinutes(1));
        tracking.addInitialMeasurement(measurement1);
        tracking.updateMeasurementTracking(measurement2, 100.0, 5.0);

        tracking.reset();

        assertEquals(0, tracking.getMeasurements().size());
        assertEquals(0, tracking.getIncreasingCount());
        assertEquals(0, tracking.getStableOrDecreasingCount());
        log.info("Reset test passed.");
    }
}

