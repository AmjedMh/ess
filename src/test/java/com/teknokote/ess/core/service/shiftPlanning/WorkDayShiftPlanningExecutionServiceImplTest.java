package com.teknokote.ess.core.service.shiftPlanning;

import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.core.dao.shifts.WorkDayShiftPlanningExecutionDao;
import com.teknokote.ess.core.service.impl.shifts.WorkDayShiftPlanningExecutionServiceImpl;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningExecutionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkDayShiftPlanningExecutionServiceImplTest {

    @InjectMocks
    private WorkDayShiftPlanningExecutionServiceImpl workDayShiftPlanningExecutionService;

    @Mock
    private ESSValidator<WorkDayShiftPlanningExecutionDto> validator;

    @Mock
    private WorkDayShiftPlanningExecutionDao dao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByWorkDay() {
        Long workDayId = 1L;
        WorkDayShiftPlanningExecutionDto expectedDto = WorkDayShiftPlanningExecutionDto.builder().id(1L).build();

        when(dao.findByWorkDay(workDayId)).thenReturn(Optional.of(expectedDto));

        Optional<WorkDayShiftPlanningExecutionDto> result = workDayShiftPlanningExecutionService.findByWorkDay(workDayId);

        assertTrue(result.isPresent());
        assertEquals(expectedDto, result.get());
        verify(dao).findByWorkDay(workDayId);
    }

    @Test
    void testFindByWorkDay_NotFound() {
        Long workDayId = 2L;

        when(dao.findByWorkDay(workDayId)).thenReturn(Optional.empty());

        Optional<WorkDayShiftPlanningExecutionDto> result = workDayShiftPlanningExecutionService.findByWorkDay(workDayId);

        assertFalse(result.isPresent());
        verify(dao).findByWorkDay(workDayId);
    }

    @Test
    void testDeleteByWorkDayShiftPlanning_WhenExists() {
        Long workDayShiftPlanningId = 1L;
        WorkDayShiftPlanningExecutionDto dto = WorkDayShiftPlanningExecutionDto.builder().id(1L).build();

        when(dao.findByWorkDay(workDayShiftPlanningId)).thenReturn(Optional.of(dto));

        workDayShiftPlanningExecutionService.deleteByWorkDayShiftPlanning(workDayShiftPlanningId);

        verify(dao).findByWorkDay(workDayShiftPlanningId);
        verify(dao).deleteById(dto.getId());
    }

    @Test
    void testDeleteByWorkDayShiftPlanning_WhenNotExists() {
        Long workDayShiftPlanningId = 1L;

        when(dao.findByWorkDay(workDayShiftPlanningId)).thenReturn(Optional.empty());

        workDayShiftPlanningExecutionService.deleteByWorkDayShiftPlanning(workDayShiftPlanningId);

        verify(dao).findByWorkDay(workDayShiftPlanningId);
        // No deletion should happen since the WorkDayShiftPlanningExecutionDto is absent
        verify(dao, never()).deleteById(anyLong());
    }
}