package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.movements.ProbeMeasurements;
import com.teknokote.ess.core.repository.ProbeMeasurementsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProbeMeasurementsServiceTest {

    @InjectMocks
    private ProbeMeasurementsService service;

    @Mock
    private ProbeMeasurementsRepository probeMeasurementsRepository;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(service);
    }

    @Test
    void testFindById() {
        Long id = 1L;
        ProbeMeasurements measurements = new ProbeMeasurements();
        measurements.setId(id);
        when(probeMeasurementsRepository.findById(id)).thenReturn(Optional.of(measurements));

        Optional<ProbeMeasurements> result = service.findById(id);

        assertNotNull(result);
        assertEquals(id, result.get().getId());
        verify(probeMeasurementsRepository).findById(id);
    }

}