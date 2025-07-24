package com.teknokote.ess.core.service.controller;

import com.teknokote.ess.controller.front.shifts.AffectedPumpAttendantController;
import com.teknokote.ess.core.service.shifts.AffectedPumpAttendantService;
import com.teknokote.ess.dto.shifts.AffectedPumpAttendantDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AffectedPumpAttendantControllerTest {

    @InjectMocks
    private AffectedPumpAttendantController affectedPumpAttendantController;

    @Mock
    private AffectedPumpAttendantService affectedPumpAttendantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddAffectedPumpAttendant() {
        AffectedPumpAttendantDto dto = AffectedPumpAttendantDto.builder().build();
        // Set properties of dto as necessary

        when(affectedPumpAttendantService.create(any(AffectedPumpAttendantDto.class))).thenReturn(dto);

        ResponseEntity<AffectedPumpAttendantDto> response = affectedPumpAttendantController.addAffectedPumpAttendant(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(affectedPumpAttendantService, times(1)).create(dto);
    }

    @Test
    void testUpdateAffectedPumpAttendant() {
        AffectedPumpAttendantDto dto = AffectedPumpAttendantDto.builder().build();
        // Set properties of dto as necessary

        when(affectedPumpAttendantService.update(any(AffectedPumpAttendantDto.class))).thenReturn(dto);

        ResponseEntity<AffectedPumpAttendantDto> response = affectedPumpAttendantController.updateAffectedPumpAttendant(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(affectedPumpAttendantService, times(1)).update(dto);
    }

    @Test
    void testGetAffectedPumpAttendant() {
        Long id = 1L;
        AffectedPumpAttendantDto dto = AffectedPumpAttendantDto.builder().build();
        // Set properties of dto as necessary

        when(affectedPumpAttendantService.checkedFindById(id)).thenReturn(dto);

        ResponseEntity<AffectedPumpAttendantDto> response = affectedPumpAttendantController.getAffectedPumpAttendant(id);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(affectedPumpAttendantService, times(1)).checkedFindById(id);
    }

    @Test
    void testListAffectedPumpAttendant() {
        AffectedPumpAttendantDto dto1 = AffectedPumpAttendantDto.builder().build();
        AffectedPumpAttendantDto dto2 = AffectedPumpAttendantDto.builder().build();
        List<AffectedPumpAttendantDto> dtos = Arrays.asList(dto1, dto2);

        when(affectedPumpAttendantService.findAll()).thenReturn(dtos);

        List<AffectedPumpAttendantDto> response = affectedPumpAttendantController.listAffectedPumpAttendant();

        assertEquals(2, response.size());
        assertEquals(dtos, response);
        verify(affectedPumpAttendantService, times(1)).findAll();
    }
}