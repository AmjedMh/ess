package com.teknokote.ess.core.service.controller;

import com.teknokote.ess.controller.front.shifts.ShiftRotationController;
import com.teknokote.ess.core.model.shifts.EnumPlanificationMode;
import com.teknokote.ess.core.service.shifts.ShiftRotationService;
import com.teknokote.ess.dto.PeriodDto;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftRotationControllerTest {

    @InjectMocks
    private ShiftRotationController shiftRotationController;
    @Mock
    private ShiftRotationService shiftRotationService;
    private ShiftRotationDto shiftRotationDto;
    private final Long stationId = 1L;
    private final Long shiftRotationId = 10L;
    String token = "";

    @BeforeEach
    void setUp() {
        shiftRotationDto = ShiftRotationDto.builder()
                .id(shiftRotationId)
                .name("Morning Shift")
                .startValidityDate(LocalDate.of(2024, 1, 1))
                .endValidityDate(LocalDate.of(2024, 12, 31))
                .stationId(stationId)
                .nbrOffDays(2)
                .planificationMode(EnumPlanificationMode.MANUEL)
                .shifts(new ArrayList<>())
                .build();
    }

    @Test
    void testAddShiftRotation() {
        when(shiftRotationService.addShiftRotation(stationId, shiftRotationDto, null))
                .thenReturn(shiftRotationDto);

        ResponseEntity<ShiftRotationDto> response = shiftRotationController.addShiftRotation(stationId, shiftRotationDto, null);

        // Verify the response and behavior
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(shiftRotationDto, response.getBody());
        verify(shiftRotationService, times(1)).addShiftRotation(stationId, shiftRotationDto, null);
    }

    @Test
    void testGetShiftRotation() {
        when(shiftRotationService.checkedFindById(stationId))
                .thenReturn(shiftRotationDto);

        ResponseEntity<ShiftRotationDto> response = shiftRotationController.getShiftRotation(stationId);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(shiftRotationDto, response.getBody());
        verify(shiftRotationService, times(1)).checkedFindById(stationId);
    }

    @Test
    void testListShiftRotation() {
        List<ShiftRotationDto> shiftRotations = Collections.singletonList(shiftRotationDto);
        when(shiftRotationService.findAllByStation(anyLong()))
                .thenReturn(shiftRotations);

        List<ShiftRotationDto> response = shiftRotationController.listShiftRotation(stationId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(shiftRotationDto, response.get(0));
        verify(shiftRotationService, times(1)).findAllByStation(anyLong());
    }

    @Test
    void testGetValidShiftRotation() {
        when(shiftRotationService.findValidRotation(stationId, LocalDate.now()))
                .thenReturn(shiftRotationDto);

        ShiftRotationDto response = shiftRotationController.getValidShiftRotation(stationId, LocalDate.now());

        assertNotNull(response);
        assertEquals(shiftRotationDto, response);
        verify(shiftRotationService, times(1)).findValidRotation(stationId, LocalDate.now());
    }

    @Test
    void testGetPeriodsByRotation() {
        PeriodDto periodDto = PeriodDto.builder().build();
        List<PeriodDto> periods = Collections.singletonList(periodDto);
        when(shiftRotationService.listPeriodTypesForStation(stationId))
                .thenReturn(periods);

        List<PeriodDto> response = shiftRotationController.getPeriodsByRotation(stationId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(periodDto, response.get(0));
        verify(shiftRotationService, times(1)).listPeriodTypesForStation(stationId);
    }

    @Test
    void testDeleteShiftRotation() {
        doNothing().when(shiftRotationService).deleteShiftRotation(stationId, shiftRotationId, null);

        shiftRotationController.delete(stationId, shiftRotationId, null);

        verify(shiftRotationService, times(1)).deleteShiftRotation(stationId, shiftRotationId, null);
    }
}
