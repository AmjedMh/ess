package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.repository.PumpPricesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PumpPriceServiceTest {

    @InjectMocks
    private PumpPriceService pumpPriceService;
    @Mock
    private PumpPricesRepository pumpPricesRepository;


    @Test
    void constructor_ShouldInjectRepository_WhenServiceIsCreated() {

        assertNotNull(pumpPriceService, "Pump Price Service should be instantiated.");
    }
}