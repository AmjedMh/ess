package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.exchanges.EnumMessageProcessingState;
import com.teknokote.ess.core.model.exchanges.EnumUploadMessageType;
import com.teknokote.ess.core.model.exchanges.MessagePtsLog;
import com.teknokote.ess.core.repository.MessagePTSLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessagePTSLogServiceTest {

    @Mock
    private MessagePTSLogRepository messagePTSLogRepository;

    @InjectMocks
    private MessagePTSLogService messagePTSLogService;

    private MessagePtsLog testLog;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testLog = new MessagePtsLog();
        testLog.setId(1L);
    }
    @Test
    void logReceivedWSSMsg_ShouldDetectMessageTypeCorrectly() {
        // Case 1: Message contains a valid known type
        String messageWithKnownType = "{\"Type\":\"UploadStatus\", \"data\":\"Test data\"}";

        // Simulate that the save method should return the testLog with an assigned ID
        when(messagePTSLogRepository.save(any(MessagePtsLog.class))).thenReturn(testLog);

        // Act
        Long result = messagePTSLogService.logReceivedWSSMsg("0027003A3438510935383135", "25-01-06T23:59:14", messageWithKnownType);

        // Assert
        assertEquals(1L, result);
    }

    @Test
    void logReceivedWSSMsg_ShouldDetectUnknownType() {
        // Case 2: Message does not contain any known types
        String messageWithUnknownType = "{\"Type\":\"UploadStatus\", \"data\":\"Test data\"}";

        // Simulate that the save method should return the testLog with an assigned ID
        when(messagePTSLogRepository.save(any(MessagePtsLog.class))).thenReturn(testLog);

        // Act
        Long result = messagePTSLogService.logReceivedWSSMsg("0027003A3438510935383135", "25-01-06T23:59:14", messageWithUnknownType);

        // Assert
        assertEquals(1L, result);
    }
    @Test
    void logWSSMsgSuccess_ShouldSaveLog() {
        messagePTSLogService.logWSSMsgSuccess("0027003A3438510935383135", "25-01-06T23:59:14", "Test success message");

        // Verify the save method was called once with the appropriate log
        verify(messagePTSLogRepository, times(1)).save(any(MessagePtsLog.class));
    }

    @Test
    void logWSSMsgFail_ShouldSaveLog() {
        messagePTSLogService.logWSSMsgFail("0027003A3438510935383135", "25-01-06T23:59:14", "Test fail message");

        // Verify the save method was called once with the appropriate log
        verify(messagePTSLogRepository, times(1)).save(any(MessagePtsLog.class));
    }

    @Test
    void logReceivedWSSMsg_ShouldReturnId() {
        when(messagePTSLogRepository.save(any(MessagePtsLog.class))).thenReturn(testLog);

        Long result = messagePTSLogService.logReceivedWSSMsg("0027003A3438510935383135", "25-01-06T23:59:14", "Test received message");

        assertEquals(1L, result);
    }

    @Test
    void logUploadFailedMsgProcessing_ShouldUpdateLogSuccessfully() {
        when(messagePTSLogRepository.findById(1L)).thenReturn(Optional.of(testLog));

        messagePTSLogService.logUploadFailedMsgProcessing(1L, new RuntimeException("Test exception"));

        assertEquals(EnumMessageProcessingState.FAIL, testLog.getProcessingState());
        assertNotNull(testLog.getFailReason());
        assertFalse(testLog.getFailReason().isEmpty());
        assertNotNull(testLog.getUpdateDate());
        verify(messagePTSLogRepository, times(1)).save(testLog);
    }

    @Test
    void logUploadFailedMsgOnUnsupportedVersion_ShouldUpdateLog() {
        when(messagePTSLogRepository.findById(1L)).thenReturn(Optional.of(testLog));

        messagePTSLogService.logUploadFailedMsgOnUnsupportedVersion(1L);

        assertEquals(EnumMessageProcessingState.FAIL, testLog.getProcessingState());
        assertEquals("UNSUPPORTED VERSION", testLog.getFailReason());
        assertNotNull(testLog.getUpdateDate());
        verify(messagePTSLogRepository, times(1)).save(testLog);
    }

    @Test
    void logUploadMsgSuccess_ShouldUpdateLogSuccessfully() {
        when(messagePTSLogRepository.findById(1L)).thenReturn(Optional.of(testLog));

        messagePTSLogService.logUploadMsgSuccess(1L);

        assertEquals(EnumMessageProcessingState.SUCCESS, testLog.getProcessingState());
        assertNotNull(testLog.getUpdateDate());
        verify(messagePTSLogRepository, times(1)).save(testLog);
    }

    @Test
    void logUploadFailedMsgReadingInformation_ShouldSaveLogWithCause() {
        Throwable cause = new RuntimeException("Test Cause");
        messagePTSLogService.logUploadFailedMsgReadingInformation("0027003A3438510935383135", "25-01-06T23:59:14", "Test message", cause);

        ArgumentCaptor<MessagePtsLog> captor = ArgumentCaptor.forClass(MessagePtsLog.class);
        verify(messagePTSLogRepository).save(captor.capture());

        MessagePtsLog savedLog = captor.getValue();
        assertEquals(EnumMessageProcessingState.FAIL_EXTRACT_INFO, savedLog.getProcessingState());
        assertNotNull(savedLog.getFailReason());
        assertFalse(savedLog.getFailReason().isEmpty());
    }

    @Test
    void findById_ShouldReturnExpectedLog() {
        when(messagePTSLogRepository.findById(1L)).thenReturn(Optional.of(testLog));

        Optional<MessagePtsLog> result = messagePTSLogService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testLog, result.get());
    }
    @Test
    void logUploadedMsgAsReceived_ShouldReturnId() {
        // Arrange
        when(messagePTSLogRepository.save(any(MessagePtsLog.class))).thenReturn(testLog);

        // Act
        Long result = messagePTSLogService.logUploadedMsgAsReceived(
                "0027003A3438510935383135",
                "25-01-06T23:59:14",
                "configId123",
                EnumUploadMessageType.UploadStatus,
                "Test uploaded message");

        // Assert
        assertEquals(1L, result);
    }
    @Test
    void logProcessingState_ShouldUpdateLogSuccessfully() {
        // Arrange
        when(messagePTSLogRepository.findById(1L)).thenReturn(Optional.of(testLog));

        // Act
        messagePTSLogService.logProcessingState(1L, EnumMessageProcessingState.SUCCESS);

        // Assert
        assertEquals(EnumMessageProcessingState.SUCCESS, testLog.getProcessingState());
        assertNotNull(testLog.getUpdateDate());
        verify(messagePTSLogRepository, times(1)).save(testLog);
    }
    @Test
    void findFailedTransactionsId_ShouldReturnFailedTransactionIds() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        when(messagePTSLogRepository.findFailedTransactions(anyString(), any(), any(), any())).thenReturn(List.of(1L, 2L));

        // Act
        List<Long> result = messagePTSLogService.findFailedTransactionsId("0027003A3438510935383135", startDate, endDate);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
    }
    @Test
    void findMeasurementByMessageTypeAndDateRange_ShouldReturnLogs() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        when(messagePTSLogRepository.findMeasurementByMessageTypeAndDateRange(startDate, endDate, "0027003A3438510935383135"))
                .thenReturn(List.of(testLog));

        // Act
        List<MessagePtsLog> result = messagePTSLogService.findMeasurementByMessageTypeAndDateRange(startDate, endDate, "0027003A3438510935383135");

        // Assert
        assertEquals(1, result.size());
        assertEquals(testLog, result.get(0));
    }
    @Test
    void logUploadFailedMsgReadingInformation_WithCauseAndMessageType_ShouldSaveLog() {
        // Arrange
        EnumUploadMessageType messageType = EnumUploadMessageType.UploadStatus;

        // Act
        messagePTSLogService.logUploadFailedMsgReadingInformation("0027003A3438510935383135", "25-01-06T23:59:14", "Test message",
                "Some error happened", messageType);

        // Assert
        ArgumentCaptor<MessagePtsLog> captor = ArgumentCaptor.forClass(MessagePtsLog.class);
        verify(messagePTSLogRepository).save(captor.capture());

        MessagePtsLog savedLog = captor.getValue();
        assertEquals(EnumMessageProcessingState.FAIL_EXTRACT_INFO, savedLog.getProcessingState());
        assertEquals("Some error happened", savedLog.getFailReason());
        assertEquals(messageType, savedLog.getMessageType());
    }
    @Test
    void logUploadFailedMsgWithUnknownConfigurationId_ShouldUpdateLog() {
        // Arrange
        when(messagePTSLogRepository.findById(1L)).thenReturn(Optional.of(testLog));

        // Act
        messagePTSLogService.logUploadFailedMsgWithUnknownConfigurationId(1L);

        // Assert
        assertEquals(EnumMessageProcessingState.UNKNOWN_CONFIGURATION, testLog.getProcessingState());
        assertNotNull(testLog.getUpdateDate());
        verify(messagePTSLogRepository, times(1)).save(testLog);
    }
}