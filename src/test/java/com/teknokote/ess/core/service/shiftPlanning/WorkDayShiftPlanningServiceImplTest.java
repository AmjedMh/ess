package com.teknokote.ess.core.service.shiftPlanning;

import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.core.dao.shifts.WorkDayShiftPlanningDao;
import com.teknokote.ess.core.service.impl.shifts.WorkDayShiftPlanningServiceImpl;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningExecutionService;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkDayShiftPlanningServiceImplTest {

    @InjectMocks
    private WorkDayShiftPlanningServiceImpl workDayShiftPlanningService;

    @Mock
    private ESSValidator<WorkDayShiftPlanningDto> validator;

    @Mock
    private WorkDayShiftPlanningDao dao;

    @Mock
    private WorkDayShiftPlanningExecutionService workDayShiftPlanningExecutionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByStationAndDay() {
        Long stationId = 1L;
        LocalDate day = LocalDate.now();
        WorkDayShiftPlanningDto expectedDto = WorkDayShiftPlanningDto.builder().id(2L).build();
        when(dao.findByStationAndDay(stationId, day)).thenReturn(expectedDto);

        WorkDayShiftPlanningDto result = workDayShiftPlanningService.findByStationAndDay(stationId, day);

        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(dao).findByStationAndDay(stationId, day);
    }

    @Test
    void testDeleteForPlannings() {
        List<Long> workDayIds = Arrays.asList(1L, 2L, 3L);

        workDayShiftPlanningService.deleteForPlannings(workDayIds);

        verify(dao).deleteForPlanning(workDayIds);
    }

    @Test
    void testHasExecutionsForRotation() {
        Long shiftRotationId = 1L;
        when(dao.hasExecutionsForRotation(shiftRotationId)).thenReturn(true);

        boolean result = workDayShiftPlanningService.hasExecutionsForRotation(shiftRotationId);

        assertTrue(result);
        verify(dao).hasExecutionsForRotation(shiftRotationId);
    }

    @Test
    void testDeleteById() {
        Long id = 1L;
        doNothing().when(workDayShiftPlanningExecutionService).deleteByWorkDayShiftPlanning(id);

        workDayShiftPlanningService.deleteById(id);

        verify(workDayShiftPlanningExecutionService).deleteByWorkDayShiftPlanning(id);
        verify(dao).deleteById(id);
    }

    @Test
    void testDeleteByStationAndRotation() {
        Long stationId = 1L;
        Long shiftRotationId = 1L;
        WorkDayShiftPlanningDto dto1 = WorkDayShiftPlanningDto.builder().id(1L).build();
        WorkDayShiftPlanningDto dto2 = WorkDayShiftPlanningDto.builder().id(2L).build();

        List<WorkDayShiftPlanningDto> planningList = Arrays.asList(dto1, dto2);

        when(dao.findByStationAndRotation(stationId, shiftRotationId)).thenReturn(planningList);

        workDayShiftPlanningService.deleteByStationAndRotation(stationId, shiftRotationId);

        verify(dao).findByStationAndRotation(stationId, shiftRotationId);
        verify(dao, times(2)).deleteById(anyLong());
    }
}