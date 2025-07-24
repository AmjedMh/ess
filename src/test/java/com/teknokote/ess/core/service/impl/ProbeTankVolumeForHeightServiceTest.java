package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.ProbeTankVolumeForHeight;
import com.teknokote.ess.core.repository.ProbeTankVolumeForHeightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProbeTankVolumeForHeightServiceTest {

    @InjectMocks
    private ProbeTankVolumeForHeightService service;
    @Mock
    private ProbeTankVolumeForHeightRepository repository;
    @Test
    void serviceShouldBeInitialized() {
        // Check if the service is not null after initialization
        assertNotNull(service);
    }

    @Test
    void testFindById() {
        Long id = 1L;
        ProbeTankVolumeForHeight volume = new ProbeTankVolumeForHeight();
        volume.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(volume));

        Optional<ProbeTankVolumeForHeight> result = service.findById(id);

        assertNotNull(result);
        verify(repository, times(1)).findById(id);
    }

}