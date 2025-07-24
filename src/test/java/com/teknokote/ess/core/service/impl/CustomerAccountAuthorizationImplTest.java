package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.events.publish.CustomerAccountAuthorizationImpl;
import com.teknokote.ess.events.publish.cm.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CustomerAccountAuthorizationImplTest {

    @InjectMocks
    private CustomerAccountAuthorizationImpl customerAccountAuthorization;

    @Mock
    private CardManagerClient cardManagerClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRequestAuthorization() {
        // Arrange
        AuthorizationRequest authorizationRequest = AuthorizationRequest.builder().build();
        AuthorizationDto expectedAuthorizationDto = AuthorizationDto.builder().build();
        when(cardManagerClient.authorize(any(AuthorizationRequest.class))).thenReturn(expectedAuthorizationDto);

        // Act
        AuthorizationDto result = customerAccountAuthorization.requestAuthorization(authorizationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAuthorizationDto, result);
        verify(cardManagerClient).authorize(authorizationRequest);
    }

    @Test
    void testSaveTransaction() {
        // Arrange
        CMTransactionDto cmTransactionDto = CMTransactionDto.builder().build();
        CMTransactionDto expectedTransactionDto = CMTransactionDto.builder().build();
        when(cardManagerClient.saveTransaction(any(CMTransactionDto.class))).thenReturn(expectedTransactionDto);

        // Act
        CMTransactionDto result = customerAccountAuthorization.saveTransaction(cmTransactionDto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTransactionDto, result);
        verify(cardManagerClient).saveTransaction(cmTransactionDto);
    }

    @Test
    void testUpdateCardStatus() {
        // Arrange
        Long cardId = 1L;
        Long authorizationId = 2L;
        Long transactionId = 3L;
        EnumCardStatus status = EnumCardStatus.AUTHORIZED;

        // Act
        customerAccountAuthorization.updateCardStatus(cardId, authorizationId, transactionId, status);

        // Assert
        verify(cardManagerClient).updateCardStatus(cardId, authorizationId, transactionId, status);
    }

    @Test
    void testGetAuthorization() {
        // Arrange
        String ptsId = "TestPtsId";
        Long pump = 1L;
        String tag = "TestTag";
        AuthorizationDto expectedAuthorizationDto = AuthorizationDto.builder().build();
        when(cardManagerClient.getAuthorization(anyString(), anyLong(), anyString())).thenReturn(expectedAuthorizationDto);

        // Act
        AuthorizationDto result = customerAccountAuthorization.getAuthorization(ptsId, pump, tag);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAuthorizationDto, result);
        verify(cardManagerClient).getAuthorization(ptsId, pump, tag);
    }
}