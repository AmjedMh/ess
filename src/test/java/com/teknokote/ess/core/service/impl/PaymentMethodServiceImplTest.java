package com.teknokote.ess.core.service.impl;

import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.core.dao.shifts.PaymentMethodDao;
import com.teknokote.ess.core.service.impl.shifts.PaymentMethodServiceImpl;
import com.teknokote.ess.dto.shifts.PaymentMethodDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceImplTest {

    @InjectMocks
    private PaymentMethodServiceImpl paymentMethodService;

    @Mock
    private ESSValidator<PaymentMethodDto> validator;

    @Mock
    private PaymentMethodDao dao;

    private List<PaymentMethodDto> paymentMethods;
    private Long stationId;

    @BeforeEach
    void setUp() {
        stationId = 1L;
        paymentMethods = new ArrayList<>();

        // Add sample PaymentMethodDto instances to the list
        PaymentMethodDto paymentMethod1 = PaymentMethodDto.builder().build();
        paymentMethod1.setId(1L);

        PaymentMethodDto paymentMethod2 = PaymentMethodDto.builder().build();
        paymentMethod2.setId(2L);

        paymentMethods.add(paymentMethod1);
        paymentMethods.add(paymentMethod2);
    }

    @Test
    void findByStationId_ShouldReturnPaymentMethods_WhenStationIdExists() {
        // Given
        when(dao.findByStationId(stationId)).thenReturn(paymentMethods);

        // When
        List<PaymentMethodDto> result = paymentMethodService.findByStationId(stationId);

        // Then
        assertEquals(2, result.size());
        assertEquals(paymentMethods, result);
        verify(dao, times(1)).findByStationId(stationId);
    }

    @Test
    void findByStationId_ShouldReturnEmptyList_WhenNoPaymentMethodsFound() {
        // Given
        when(dao.findByStationId(stationId)).thenReturn(new ArrayList<>());

        // When
        List<PaymentMethodDto> result = paymentMethodService.findByStationId(stationId);

        // Then
        assertEquals(0, result.size());
        verify(dao, times(1)).findByStationId(stationId);
    }
    @Test
    void findByStationId_ShouldHandleDaoException() {
        // Given
        doThrow(new RuntimeException("Database exception")).when(dao).findByStationId(stationId);

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            paymentMethodService.findByStationId(stationId);
        });

        assertEquals("Database exception", exception.getMessage());
    }
}