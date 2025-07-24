package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.UserHistoryDao;
import com.teknokote.ess.core.model.EnumActivityType;
import com.teknokote.ess.dto.UserHistoryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHistoryServiceImplTest {

    @InjectMocks
    private UserHistoryServiceImpl userHistoryService;
    @Mock
    private UserHistoryDao userHistoryDao;
    private UserHistoryDto sampleUserHistoryDto1;
    private UserHistoryDto sampleUserHistoryDto2;
    private UserHistoryDto sampleUserHistoryDto3;

    @BeforeEach
    void setUp() {
        sampleUserHistoryDto1 = UserHistoryDto.builder()
                .id(1L)
                .activityType(EnumActivityType.POST)
                .action("Created a resource")
                .activityDate(LocalDateTime.now().minusDays(1))
                .build();

        sampleUserHistoryDto2 = UserHistoryDto.builder()
                .id(2L)
                .activityType(EnumActivityType.GET)
                .action("Retrieved a resource")
                .activityDate(LocalDateTime.now().minusDays(2))
                .build();

        sampleUserHistoryDto3 = UserHistoryDto.builder()
                .id(3L)
                .activityType(EnumActivityType.PUT)
                .action("Updated a resource")
                .activityDate(LocalDateTime.now().minusDays(3))
                .build();
    }

    @Test
    void userLogs_ShouldReturnFilteredLogs_WhenValidUserIdAndDateRangeProvided() {
        // Given
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(5);
        LocalDateTime endDate = LocalDateTime.now();
        when(userHistoryDao.findAllById(userId)).thenReturn(Arrays.asList(sampleUserHistoryDto1, sampleUserHistoryDto2, sampleUserHistoryDto3));

        // When
        List<UserHistoryDto> result = userHistoryService.userLogs(userId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(sampleUserHistoryDto1.getId(), result.get(0).getId()); // Created log should be first
        assertEquals(sampleUserHistoryDto3.getId(), result.get(1).getId()); // Updated log should be second
    }

    @Test
    void userLogs_ShouldIgnoreGetActivityType_WhenFiltering() {
        // Given
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(5);
        LocalDateTime endDate = LocalDateTime.now();
        when(userHistoryDao.findAllById(userId)).thenReturn(Arrays.asList(sampleUserHistoryDto1, sampleUserHistoryDto2, sampleUserHistoryDto3));

        // When
        List<UserHistoryDto> result = userHistoryService.userLogs(userId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // GET activity should be excluded
        assertFalse(result.contains(sampleUserHistoryDto2));
    }

    @Test
    void userLogs_ShouldReturnEmptyList_WhenNoLogsMatchCriteria() {
        // Given
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().plusDays(1); // Future date range
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);
        when(userHistoryDao.findAllById(userId)).thenReturn(Arrays.asList(sampleUserHistoryDto1, sampleUserHistoryDto2, sampleUserHistoryDto3));

        // When
        List<UserHistoryDto> result = userHistoryService.userLogs(userId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty()); // No logs should match
    }

    @Test
    void userLogs_ShouldReturnAllLogs_WhenNoDateRangeProvided() {
        // Given
        Long userId = 1L;
        when(userHistoryDao.findAllById(userId)).thenReturn(Arrays.asList(sampleUserHistoryDto1, sampleUserHistoryDto2, sampleUserHistoryDto3));

        // When
        List<UserHistoryDto> result = userHistoryService.userLogs(userId, null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // GET activity should be excluded
        assertTrue(result.contains(sampleUserHistoryDto1)); // Should include CREATE
        assertTrue(result.contains(sampleUserHistoryDto3)); // Should include UPDATE
    }
}