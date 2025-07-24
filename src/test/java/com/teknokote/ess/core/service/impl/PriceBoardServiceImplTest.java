package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.Pricebord;
import com.teknokote.ess.core.repository.PricebordRepository;
import com.teknokote.pts.client.response.priceboard.PriceBoardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceBoardServiceImplTest {

    @InjectMocks
    private PriceBoardServiceImpl priceBoardService;
    @Mock
    private PricebordRepository pricebordRepository;
    private PriceBoardStatus priceBoardStatus;

    @BeforeEach
    void setUp() {
        priceBoardStatus = PriceBoardStatus.builder().build();
        priceBoardStatus.setPriceBoard(100L);
        priceBoardStatus.setOnline(true);
        priceBoardStatus.setError(Boolean.valueOf("None"));
    }

    @Test
    void addPriceBoard_ShouldSavePricebord_WhenCalled() {
        // Given
        Pricebord expectedPricebord = new Pricebord();
        expectedPricebord.setPrice(priceBoardStatus.getPriceBoard());
        expectedPricebord.setOnline(priceBoardStatus.getOnline());
        expectedPricebord.setError(priceBoardStatus.getError());

        when(pricebordRepository.save(any(Pricebord.class))).thenReturn(expectedPricebord);

        // When
        Pricebord result = priceBoardService.addPriceBoard(priceBoardStatus);

        // Then
        assertNotNull(result);
        assertEquals(expectedPricebord.getPrice(), result.getPrice());
        assertEquals(expectedPricebord.getOnline(), result.getOnline());
        assertEquals(expectedPricebord.getError(), result.getError());
        verify(pricebordRepository, times(1)).save(any(Pricebord.class));
    }
}