package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.core.dao.ShiftDao;
import com.teknokote.ess.dto.ShiftDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ShiftServiceImplTest {

    @InjectMocks
    private ShiftServiceImpl shiftServiceImpl;

    @Mock
    private ShiftDao shiftDao;

    @Mock
    private ESSValidator<ShiftDto> validator;
    @Mock
    private ShiftDto shift;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindOffDay_NoOffDayShifts() {
        // Arrange
        when(shiftDao.findOffDay()).thenReturn(Collections.emptyList());

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
            shiftServiceImpl.findOffDay();
        });
        assertEquals("There is no defined off day shift", exception.getMessage());
    }

    @Test
    void testFindOffDay_MoreThanOneOffDayShift() {
        // Arrange
        shift = ShiftDto.builder().build(); // create a sample ShiftDto
        List<ShiftDto> offDayShifts = List.of(shift, shift);
        when(shiftDao.findOffDay()).thenReturn(offDayShifts);

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
            shiftServiceImpl.findOffDay();
        });
        assertEquals("There is more one defined off day shift", exception.getMessage());
    }

    @Test
    void testFindOffDay_OneOffDayShift() {
        // Arrange
        shift = ShiftDto.builder().build(); // create a sample ShiftDto
        List<ShiftDto> offDayShifts = List.of(shift);
        when(shiftDao.findOffDay()).thenReturn(offDayShifts);

        // Act
        ShiftDto result = shiftServiceImpl.findOffDay();

        // Assert
        assertEquals(shift, result);
    }
}