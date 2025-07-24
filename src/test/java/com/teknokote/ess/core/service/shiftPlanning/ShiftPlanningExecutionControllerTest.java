package com.teknokote.ess.core.service.shiftPlanning;

import com.teknokote.ess.controller.front.shifts.ShiftPlanningExecutionController;
import com.teknokote.ess.core.service.shifts.ShiftPlanningExecutionService;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningExecutionDto;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDto;
import com.teknokote.ess.dto.shifts.ShiftDetailUpdatesDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDto;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class ShiftPlanningExecutionControllerTest {

    @InjectMocks
    private ShiftPlanningExecutionController shiftPlanningExecutionController;
    @Mock
    private ShiftPlanningExecutionService shiftPlanningExecutionService;
    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateShiftPlanningExecution_ShouldReturnCreated() {
        // Arrange
        Long stationId = 1L;
        LocalDate day = LocalDate.now();
        List<DynamicShiftPlanningExecutionDto> expected = new ArrayList<>();
        when(shiftPlanningExecutionService.generate(day, stationId)).thenReturn(expected);

        // Act
        ResponseEntity<List<DynamicShiftPlanningExecutionDto>> response = shiftPlanningExecutionController.generateShiftPlanningExecution(stationId, day);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(shiftPlanningExecutionService).generate(day, stationId);
    }

    @Test
    void startShiftPlanningExecution_ShouldReturnCreated() {
        // Arrange
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 2L;
        LocalDateTime startDateTime = LocalDateTime.now();
        ShiftPlanningExecutionDto expectedDto = ShiftPlanningExecutionDto.builder()
                .id(1L)
                .version(1L)
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .pumpAttendantCollectionSheets(new ArrayList<>())
                .build();
        when(shiftPlanningExecutionService.start(eq(stationId), eq(shiftPlanningExecutionId), eq(startDateTime), any())).thenReturn(expectedDto);

        // Act
        ResponseEntity<ShiftPlanningExecutionDto> response = shiftPlanningExecutionController.startShiftPlanningExecution(stationId, shiftPlanningExecutionId, startDateTime, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedDto);
        verify(shiftPlanningExecutionService).start(eq(stationId), eq(shiftPlanningExecutionId), eq(startDateTime), eq(request));
    }

    @Test
    void stopShiftPlanningExecution_ShouldReturnCreated() {
        // Arrange
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 2L;
        LocalDateTime endDateTime = LocalDateTime.now();
        ShiftPlanningExecutionDto expectedDto = ShiftPlanningExecutionDto.builder()
                .id(1L)
                .version(1L)
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .pumpAttendantCollectionSheets(new ArrayList<>())
                .build();
        when(shiftPlanningExecutionService.stop(eq(stationId), eq(shiftPlanningExecutionId), eq(endDateTime), any())).thenReturn(expectedDto);

        // Act
        ResponseEntity<ShiftPlanningExecutionDto> response = shiftPlanningExecutionController.stopShiftPlanningExecution(stationId, shiftPlanningExecutionId, endDateTime, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedDto);
        verify(shiftPlanningExecutionService).stop(eq(stationId), eq(shiftPlanningExecutionId), eq(endDateTime), eq(request));
    }

    @Test
    void unlockShiftPlanningExecution_ShouldReturnCreated() {
        // Arrange
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 2L;
        ShiftPlanningExecutionDto expectedDto = ShiftPlanningExecutionDto.builder()
                .id(1L)
                .version(1L)
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .pumpAttendantCollectionSheets(new ArrayList<>())
                .build();
        when(shiftPlanningExecutionService.unlock(eq(stationId), eq(shiftPlanningExecutionId), any())).thenReturn(expectedDto);

        // Act
        ResponseEntity<ShiftPlanningExecutionDto> response = shiftPlanningExecutionController.unlockShiftPlanningExecution(stationId, shiftPlanningExecutionId, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedDto);
        verify(shiftPlanningExecutionService).unlock(eq(stationId), eq(shiftPlanningExecutionId), eq(request));
    }

    @Test
    void lockShiftPlanningExecution_ShouldReturnCreated() {
        // Arrange
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 2L;
        ShiftPlanningExecutionDto expectedDto = ShiftPlanningExecutionDto.builder()
                .id(1L)
                .version(1L)
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .pumpAttendantCollectionSheets(new ArrayList<>())
                .build();
        when(shiftPlanningExecutionService.lock(eq(stationId), eq(shiftPlanningExecutionId), any())).thenReturn(expectedDto);

        // Act
        ResponseEntity<ShiftPlanningExecutionDto> response = shiftPlanningExecutionController.lockShiftPlanningExecution(stationId, shiftPlanningExecutionId, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedDto);
        verify(shiftPlanningExecutionService).lock(eq(stationId), eq(shiftPlanningExecutionId), eq(request));
    }

    @Test
    void updatePumpAttendantSheet_ShouldReturnCreated() {
        // Arrange
        PumpAttendantCollectionSheetDto dto = PumpAttendantCollectionSheetDto.builder().build();
        PumpAttendantCollectionSheetDto expectedDto = PumpAttendantCollectionSheetDto.builder().build();
        when(shiftPlanningExecutionService.updateCollectionSheetForPumpAttendant(any(), any())).thenReturn(expectedDto);

        // Act
        ResponseEntity<PumpAttendantCollectionSheetDto> response = shiftPlanningExecutionController.createPumpAttendantSheet(dto, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedDto);
        verify(shiftPlanningExecutionService).updateCollectionSheetForPumpAttendant(eq(dto), eq(request));
    }

    @Test
    void updateShiftPlanningExecution_ShouldReturnCreated() {
        // Arrange
        ShiftPlanningExecutionDto dto = ShiftPlanningExecutionDto.builder()
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .pumpAttendantCollectionSheets(new ArrayList<>())
                .build();
        ShiftPlanningExecutionDto expectedDto = ShiftPlanningExecutionDto.builder()
                .id(1L)
                .version(1L)
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .pumpAttendantCollectionSheets(new ArrayList<>())
                .build();
        when(shiftPlanningExecutionService.update(any())).thenReturn(expectedDto);

        // Act
        ResponseEntity<ShiftPlanningExecutionDto> response = shiftPlanningExecutionController.updateShiftPlanningExecution(dto);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedDto);
        verify(shiftPlanningExecutionService).update(eq(dto));
    }

    @Test
    void updateShiftPlanningExecutionDetail_ShouldReturnOk() {
        // Arrange
        Long stationId = 1L;
        ShiftDetailUpdatesDto dto = ShiftDetailUpdatesDto.builder().build();
        ShiftPlanningExecutionDto expectedDto = ShiftPlanningExecutionDto.builder()
                .id(1L)
                .version(1L)
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .pumpAttendantCollectionSheets(new ArrayList<>())
                .build();
        when(shiftPlanningExecutionService.updateShiftDetails(any(), any())).thenReturn(expectedDto);

        // Act
        ResponseEntity<ShiftPlanningExecutionDto> response = shiftPlanningExecutionController.updateShiftPlanningExecutionDetail(stationId, dto, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedDto);
        verify(shiftPlanningExecutionService).updateShiftDetails(eq(dto), eq(request));
    }
    @Test
    void getShiftPlanningExecution_ShouldReturnCreated() {
        // Arrange
        Long id = 1L;
        ShiftPlanningExecutionDto expectedDto = ShiftPlanningExecutionDto.builder()
                .id(id)
                .version(1L)
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .pumpAttendantCollectionSheets(new ArrayList<>())
                .build();

        when(shiftPlanningExecutionService.checkedFindById(id)).thenReturn(expectedDto);

        // Act
        ResponseEntity<ShiftPlanningExecutionDto> response = shiftPlanningExecutionController.getShiftPlanningExecution(id);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedDto);
        verify(shiftPlanningExecutionService).checkedFindById(id);
    }
    @Test
    void listShiftPlanningExecution_ShouldReturnList() {
        // Arrange
        List<ShiftPlanningExecutionDto> expected = new ArrayList<>();
        when(shiftPlanningExecutionService.findAll()).thenReturn(expected);

        // Act
        List<ShiftPlanningExecutionDto> response = shiftPlanningExecutionController.listShiftPlanningExecution();

        // Assert
        assertThat(response).isEqualTo(expected);
        verify(shiftPlanningExecutionService).findAll();
    }

    @Test
    void calculateTotalAmountPumpAttendant_ShouldReturnAmount() {
        // Arrange
        Long id = 1L;
        Long pumpAttendantId = 2L;
        BigDecimal expectedAmount = BigDecimal.valueOf(100.00);
        when(shiftPlanningExecutionService.calculateTotalAmountPumpAttendant(id, pumpAttendantId)).thenReturn(expectedAmount);

        // Act
        BigDecimal response = shiftPlanningExecutionController.calculateTotalAmountPumpAttendant(id, pumpAttendantId);

        // Assert
        assertThat(response).isEqualTo(expectedAmount);
        verify(shiftPlanningExecutionService).calculateTotalAmountPumpAttendant(id, pumpAttendantId);
    }
}