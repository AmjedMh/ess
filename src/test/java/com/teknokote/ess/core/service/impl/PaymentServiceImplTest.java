package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.Payment;
import com.teknokote.ess.core.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Test
    void serviceShouldBeInitialized() {
        // Check if the service is not null after initialization
        assertNotNull(paymentService);
    }

    @Test
    void testFindById() {
        Long id = 1L;
        Payment payment = new Payment();
        payment.setId(id);
        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(paymentRepository, times(1)).findById(id);
    }

    @Test
    void testFindById_notFound() {
        Long id = 1L;
        when(paymentRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Payment> result = paymentService.findById(id);

        assertFalse(result.isPresent());
        verify(paymentRepository, times(1)).findById(id);
    }

}