package com.teknokote.ess.core.service.shiftPlanning;

import com.teknokote.ess.controller.front.shifts.ShiftPlanningController;
import com.teknokote.ess.core.service.shifts.ShiftPlanningService;
import com.teknokote.ess.dto.ShiftDto;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import com.teknokote.ess.dto.shifts.AffectedPumpAttendantDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class ShiftPlanningControllerTest {

    @InjectMocks
    private ShiftPlanningController shiftPlanningController;
    @Mock
    private ShiftPlanningService shiftPlanningService;
    private ShiftPlanningDto shiftPlanningDto;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize ShiftDto
        ShiftDto shiftDto = ShiftDto.builder()
                .id(1L)
                .version(1L)
                .index(0)
                .name("Morning Shift")
                .startingTime(LocalTime.of(9, 0))
                .endingTime(LocalTime.of(17, 0))
                .shiftRotationId(1L)
                .offDay(false)
                .crossesDayBoundary(false)
                .build();

        // Initialize AffectedPumpAttendantDto instances if necessary
        Set<AffectedPumpAttendantDto> affectedPumpAttendants = new HashSet<>();
        AffectedPumpAttendantDto attendant = AffectedPumpAttendantDto.builder()
                .id(1L)
                .pumpAttendantId(1L)
                .pumpId(1L)
                .pumpAttendantTeamId(1L)
                .build();
        affectedPumpAttendants.add(attendant);

        // Initialize PumpAttendantTeamDto
        PumpAttendantTeamDto pumpAttendantTeamDto = PumpAttendantTeamDto.builder()
                .id(1L)
                .version(1L)
                .stationId(1L)
                .shiftRotationId(1L)
                .name("Team A")
                .affectedPumpAttendant(affectedPumpAttendants)
                .build();

        // Initialize ShiftPlanningDto using the above DTOs
        shiftPlanningDto = ShiftPlanningDto.builder()
                .id(1L)
                .version(1L)
                .index(0)
                .stationId(1L)
                .shiftId(1L)
                .shift(shiftDto)
                .shiftRotationId(1L)
                .workDayShiftPlanningId(1L)
                .pumpAttendantTeamId(1L)
                .pumpAttendantTeam(pumpAttendantTeamDto)
                .hasExecution(false)
                .build();
    }

    @Test
    void testAddShiftPlanning() {
        when(shiftPlanningService.create(any(ShiftPlanningDto.class))).thenReturn(shiftPlanningDto);

        ResponseEntity<ShiftPlanningDto> response = shiftPlanningController.addShiftPlanning(shiftPlanningDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(shiftPlanningDto, response.getBody());
        verify(shiftPlanningService, times(1)).create(shiftPlanningDto);
    }

    @Test
    void testGenerateShiftPlanning() {
        when(shiftPlanningService.generatePlanning(anyLong(), any(), any())).thenReturn(Collections.singletonList(shiftPlanningDto));
        when(shiftPlanningService.createList(anyList())).thenReturn(Collections.singletonList(shiftPlanningDto));

        ResponseEntity<List<ShiftPlanningDto>> response = shiftPlanningController.generateShiftPlanning(1L, LocalDate.now(), mock(HttpServletRequest.class));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(shiftPlanningService, times(1)).deleteShiftPlanning(anyLong(), any());
    }

    @Test
    void testResetShiftPlanning() {
        when(shiftPlanningService.resetShiftPlanning(anyLong(), any(), any())).thenReturn(Collections.singletonList(shiftPlanningDto));

        ResponseEntity<List<ShiftPlanningDto>> response = shiftPlanningController.resetShiftPlanning(1L, LocalDate.now(), mock(HttpServletRequest.class));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testUpdateShiftPlanning() {
        when(shiftPlanningService.update(any(ShiftPlanningDto.class))).thenReturn(shiftPlanningDto);

        ResponseEntity<ShiftPlanningDto> response = shiftPlanningController.updateShiftPlanning(shiftPlanningDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(shiftPlanningDto, response.getBody());
        verify(shiftPlanningService, times(1)).update(shiftPlanningDto);
    }

    @Test
    void testUpdateShiftPlanningList() {
        when(shiftPlanningService.updateList(anyList())).thenReturn(Collections.singletonList(shiftPlanningDto));

        ResponseEntity<List<ShiftPlanningDto>> response = shiftPlanningController.updateShiftPlanningList(Collections.singletonList(shiftPlanningDto));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetShiftPlanning() {
        when(shiftPlanningService.checkedFindById(anyLong())).thenReturn(shiftPlanningDto);

        ResponseEntity<ShiftPlanningDto> response = shiftPlanningController.getShiftPlanning(1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(shiftPlanningDto, response.getBody());
    }

    @Test
    void testListShiftPlanning() {
        when(shiftPlanningService.findByStationAndMonth(anyLong(), any())).thenReturn(Collections.singletonList(shiftPlanningDto));
        when(shiftPlanningService.mapToDynamicDto(any())).thenReturn(new DynamicShiftPlanningDto());

        List<DynamicShiftPlanningDto> response = shiftPlanningController.listShiftPlanning(1L, LocalDate.now());

        assertNotNull(response);
        assertEquals(1, response.size());
    }
}