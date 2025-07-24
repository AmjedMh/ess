package com.teknokote.ess.core.service.shiftPlanning;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.core.dao.StationDao;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningDao;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningExecutionDao;
import com.teknokote.ess.core.dao.shifts.WorkDayShiftPlanningDao;
import com.teknokote.ess.core.service.ShiftService;
import com.teknokote.ess.core.service.impl.shifts.ShiftPlanningServiceImpl;
import com.teknokote.ess.core.service.organization.PumpAttendantTeamService;
import com.teknokote.ess.core.service.shifts.ShiftRotationService;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningService;
import com.teknokote.ess.dto.ShiftDto;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningDto;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ShiftPlanningServiceImplTest
{
   private PumpAttendantTeamService pumpAttendantTeamService;
   private ShiftRotationService shiftRotationService;
   private ShiftPlanningServiceImpl shiftPlanningService;
   private ShiftPlanningDao shiftPlanningDao;
   private ShiftService shiftService;
   private WorkDayShiftPlanningDao workDayShiftPlanningDao;
   private WorkDayShiftPlanningService workDayShiftPlanningService;
   private ShiftPlanningExecutionDao executionDao;
   private StationDao stationDao;
   @Mock
   private HttpServletRequest request;
   @Mock
   private ApplicationEventPublisher eventPublisher;

   private Long stationId = 1L;
   private LocalDate month = LocalDate.of(2025, 3, 1);

   @BeforeEach
   void init() {
      // Mocking dependencies
      pumpAttendantTeamService = mock(PumpAttendantTeamService.class);
      workDayShiftPlanningService = mock(WorkDayShiftPlanningService.class);
      shiftRotationService = mock(ShiftRotationService.class);
      shiftPlanningDao = mock(ShiftPlanningDao.class);
      shiftService = mock(ShiftService.class);
      workDayShiftPlanningDao = mock(WorkDayShiftPlanningDao.class);
      executionDao = mock(ShiftPlanningExecutionDao.class);
      stationDao = mock(StationDao.class);
      eventPublisher = mock(ApplicationEventPublisher.class);

      // Initializing ShiftPlanningServiceImpl
      shiftPlanningService = new ShiftPlanningServiceImpl();

      // Inject mocked dependencies
      shiftPlanningService.setStationDao(stationDao);
      shiftPlanningService.setShiftService(shiftService);
      shiftPlanningService.setDao(shiftPlanningDao);
      shiftPlanningService.setShiftRotationService(shiftRotationService);
      shiftPlanningService.setPumpAttendantTeamService(pumpAttendantTeamService);
      shiftPlanningService.setWorkDayShiftPlanningDao(workDayShiftPlanningDao);
      shiftPlanningService.setExecutionDao(executionDao);
      shiftPlanningService.setEventPublisher(eventPublisher);
      shiftPlanningService.setWorkDayShiftPlanningService(workDayShiftPlanningService);

      Authentication authentication = mock(Authentication.class);
      SecurityContextHolder.getContext().setAuthentication(authentication);
   }

   @Test
   void generate()
   {
      List<PumpAttendantTeamDto> teams=List.of(PumpAttendantTeamDto.builder().id(1L).stationId(1L).name("A").build(),
         PumpAttendantTeamDto.builder().id(2L).stationId(1L).name("B").build(),
         PumpAttendantTeamDto.builder().id(3L).stationId(1L).name("C").build(),
         PumpAttendantTeamDto.builder().id(4L).stationId(1L).name("D").build());
      when(pumpAttendantTeamService.findByRotation(any(),any())).thenReturn(teams);
      ShiftRotationDto shiftRotationDto =ShiftRotationDto.builder().id(10L)
         .shifts(List.of(ShiftDto.builder().id(20L).shiftRotationId(10L).index(1).name("P1").build(),
            ShiftDto.builder().id(21L).shiftRotationId(10L).index(2).name("P2").build(),
            ShiftDto.builder().id(22L).shiftRotationId(10L).index(3).name("P3").build()))
         .build();
      when(shiftRotationService.findValidRotation(any(),any())).thenReturn(shiftRotationDto);
      when(shiftPlanningDao.findByStationAndPeriod(any(),any(),any())).thenReturn(List.of());
      final ShiftDto offDay = ShiftDto.createOffDay();
      offDay.setId(23L);
      when(shiftService.findOffDay()).thenReturn(offDay);
      WorkDayShiftPlanningDto workDayShiftPlanningDto=WorkDayShiftPlanningDto.builder().id(1L).build();
      when(workDayShiftPlanningDao.create(any())).thenReturn(workDayShiftPlanningDto);

      final List<ShiftPlanningDto> generatedPlanning = shiftPlanningService.generate(1L, LocalDate.now().withDayOfMonth(1),LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()), 2);
      generatedPlanning.forEach(System.out::println);
      assertFalse(generatedPlanning.isEmpty(), "Generated planning should not be empty");
   }

   @Test
   void testGenerateShiftPlannings() {
      // Mock data
      ShiftRotationDto rotation = ShiftRotationDto.builder()
              .id(10L)
              .stationId(1L)
              .build();

      List<ShiftDto> shifts = List.of(
              ShiftDto.builder().id(20L).name("Shift 1").build(),
              ShiftDto.builder().id(21L).name("Shift 2").build(),
              ShiftDto.builder().id(22L).name("Shift 3").build()
      );

      List<PumpAttendantTeamDto> lastDayPlanning = List.of(
              PumpAttendantTeamDto.builder().id(1L).build(),
              PumpAttendantTeamDto.builder().id(2L).build(),
              PumpAttendantTeamDto.builder().id(3L).build()
      );

      LocalDate date = LocalDate.now();

      WorkDayShiftPlanningDto workDayShiftPlanningDto = WorkDayShiftPlanningDto.builder()
              .id(100L)
              .build();

      when(workDayShiftPlanningDao.create(any())).thenReturn(workDayShiftPlanningDto);

      // Call the method
      List<ShiftPlanningDto> shiftPlannings = shiftPlanningService.generateShiftPlannings(rotation, shifts, lastDayPlanning, date);

      // Assertions
      assertNotNull(shiftPlannings, "Shift planning list should not be null");
      assertFalse(shiftPlannings.isEmpty(), "Shift planning list should not be empty");
      assertEquals(shifts.size() - 1, shiftPlannings.size(), "Shift planning should be created for all shifts except the last one");

      for (int i = 0; i < shiftPlannings.size(); i++) {
         ShiftPlanningDto shiftPlanning = shiftPlannings.get(i);
         assertEquals(shifts.get(i).getId(), shiftPlanning.getShiftId(), "Shift ID should match");
         assertEquals(rotation.getId(), shiftPlanning.getShiftRotationId(), "Shift rotation ID should match");
         assertEquals(workDayShiftPlanningDto.getId(), shiftPlanning.getWorkDayShiftPlanningId(), "WorkDayShiftPlanning ID should match");
         assertEquals(i, shiftPlanning.getIndex(), "Index should match");
         assertEquals(Boolean.FALSE, shiftPlanning.getHasExecution(), "Has execution should be false");
      }

      // Verify interactions
      verify(workDayShiftPlanningDao, times(1)).create(any());
   }
   @Test
   void deletePlanning_shouldDeleteSuccessfully() {
      // Arrange
      List<ShiftPlanningDto> shiftPlannings = List.of(
              ShiftPlanningDto.builder().id(1L).build(),
              ShiftPlanningDto.builder().id(2L).build()
      );

      List<Long> expectedIds = shiftPlannings.stream()
              .map(ShiftPlanningDto::getId)
              .toList();

      // Act
      shiftPlanningService.deletePlanning(shiftPlannings);

      // Assert
      verify(shiftPlanningDao, times(1)).deleteAllByIdInBatch(expectedIds);
   }

   @Test
   void deletePlanning_shouldThrowExceptionOnFailure() {
      // Arrange
      List<ShiftPlanningDto> shiftPlannings = List.of(ShiftPlanningDto.builder().id(1L).build());

      doThrow(new RuntimeException("Database error")).when(shiftPlanningDao)
              .deleteAllByIdInBatch(anyList());

      // Act & Assert
      ServiceValidationException thrown = assertThrows(
              ServiceValidationException.class,
              () -> shiftPlanningService.deletePlanning(shiftPlannings)
      );

      assertEquals("Could not delete shift planning! Check if an existing shift planning execution is running.", thrown.getMessage());
   }

   @Test
   void testResetShiftPlanning_NoShiftPlannings() {
      // Arrange
      when(shiftPlanningService.findByStationAndMonth(stationId, month)).thenReturn(Collections.emptyList());

      // Act
      List<ShiftPlanningDto> result = shiftPlanningService.resetShiftPlanning(stationId, month, request);

      // Assert
      assertTrue(result.isEmpty(), "Expected empty result when no shift planning found");
   }

   @Test
   void testResetShiftPlanning_WithExecutions() {
      // Arrange
      ShiftPlanningDto shiftPlanningDto = mock(ShiftPlanningDto.class);
      when(shiftPlanningDto.getHasExecution()).thenReturn(true);
      List<ShiftPlanningDto> shiftPlanningList = List.of(shiftPlanningDto);
      when(shiftPlanningService.findByStationAndMonth(stationId, month)).thenReturn(shiftPlanningList);

      // Act & Assert
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         shiftPlanningService.resetShiftPlanning(stationId, month, request);
      });
      assertEquals("You can't reinitialise this shift planning because there are one or more running executions. You need to create another rotation", exception.getMessage());
   }

   @Test
   void testGeneratePlanning_StationNotFound() {
      // Arrange
      when(stationDao.findById(stationId)).thenReturn(Optional.empty());

      // Act & Assert
      EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
         shiftPlanningService.generatePlanning(stationId, month, request);
      });
      assertEquals("Station not found with ID: 1", thrown.getMessage());
   }
   @Test
   void testDeleteShiftPlanning_NoShiftPlannings() {
      // Arrange
      when(shiftPlanningService.findByStationAndMonth(stationId, month)).thenReturn(Collections.emptyList());

      // Act
      shiftPlanningService.deleteShiftPlanning(stationId, month);

      // Assert
      verify(executionDao, never()).deleteForPlannings(any());
   }
   @Test
   void testDeleteShiftPlanning_WithExecutions() {
      // Arrange
      ShiftPlanningDto mockShiftPlanningDto = mock(ShiftPlanningDto.class);
      when(mockShiftPlanningDto.getHasExecution()).thenReturn(true);
      List<ShiftPlanningDto> shiftPlanningList = List.of(mockShiftPlanningDto);
      when(shiftPlanningService.findByStationAndMonth(stationId, month)).thenReturn(shiftPlanningList);

      // Act & Assert
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         shiftPlanningService.deleteShiftPlanning(stationId, month);
      });
      assertEquals("You can't reinitialise this shift planning because there are one or more running executions. You need to create another rotation", exception.getMessage());
   }
   @Test
   void testGetDynamicShiftPlanning_ValidStationId() {
      // Arrange
      ShiftPlanningDto mockShiftPlanningDto = ShiftPlanningDto.builder()
              .id(1L)
              .workDayShiftPlanningId(100L)
              .build();

      // Create mock WorkDayShiftPlanningDto
      WorkDayShiftPlanningDto mockWorkDayShiftPlanningDto = WorkDayShiftPlanningDto.builder()
              .id(100L)
              .day(LocalDate.of(2025, 3, 1))
              .build();

      // Set up mocks to respond correctly
      when(shiftPlanningDao.findAllByStation(stationId)).thenReturn(List.of(mockShiftPlanningDto));
      when(workDayShiftPlanningService.checkedFindById(100L)).thenReturn(mockWorkDayShiftPlanningDto);

      // Act
      List<DynamicShiftPlanningDto> result = shiftPlanningService.getDynamicShiftPlannig(stationId);

      // Assert
      assertNotNull(result);
      assertFalse(result.isEmpty(), "Dynamic shift planning should not be empty.");
      verify(shiftPlanningDao, times(1)).findAllByStation(stationId);
   }
   @Test
   void testDeleteWorkDays_WithShiftPlannings() {
      // Arrange
      Long workDayId1 = 1L;
      Long workDayId2 = 2L;
      ShiftPlanningDto shiftPlanning1 = ShiftPlanningDto.builder().workDayShiftPlanningId(workDayId1).build();
      ShiftPlanningDto shiftPlanning2 = ShiftPlanningDto.builder().workDayShiftPlanningId(workDayId2).build();
      List<ShiftPlanningDto> shiftPlannings = Arrays.asList(shiftPlanning1, shiftPlanning2);

      // Act
      shiftPlanningService.deleteWorkDays(shiftPlannings);

      // Assert
      verify(workDayShiftPlanningDao, times(1)).deleteForPlanning(Arrays.asList(workDayId1, workDayId2));
   }

   @Test
   void testDeleteWorkDays_WithoutShiftPlannings() {
      // Arrange
      List<ShiftPlanningDto> shiftPlannings = Collections.emptyList();

      // Act
      shiftPlanningService.deleteWorkDays(shiftPlannings);

      // Assert
      verify(workDayShiftPlanningDao, times(1)).deleteForPlanning(Collections.emptyList());
   }
   @Test
   void testDeleteShiftPlanning_WhenNoExecutions() {
      // Arrange
      stationId = 1L;
      month = LocalDate.of(2023, 10, 1);

      // Mock a list of ShiftPlanningDto with no executions
      ShiftPlanningDto planning1 = ShiftPlanningDto.builder().id(1L).hasExecution(false).build();
      ShiftPlanningDto planning2 = ShiftPlanningDto.builder().id(2L).hasExecution(false).build();
      List<ShiftPlanningDto> shiftPlanningList = Arrays.asList(planning1, planning2);

      // Mock the behavior of findByStationAndMonth
      when(shiftPlanningService.findByStationAndMonth(stationId, month)).thenReturn(shiftPlanningList);

      // Act
      shiftPlanningService.deleteShiftPlanning(stationId, month);

      // Assert
      verify(executionDao, times(1)).deleteForPlannings(Arrays.asList(1L, 2L));
   }

   @Test
   void testDeleteShiftPlanning_WhenExecutionsExist() {
      // Arrange
      stationId = 1L;
      month = LocalDate.of(2023, 10, 1);

      // Mock a list of ShiftPlanningDto with one execution
      ShiftPlanningDto planning1 = ShiftPlanningDto.builder().id(1L).hasExecution(false).build();
      ShiftPlanningDto planning2 = ShiftPlanningDto.builder().id(2L).hasExecution(true).build();
      List<ShiftPlanningDto> shiftPlanningList = Arrays.asList(planning1, planning2);

      // Mock the behavior of findByStationAndMonth
      when(shiftPlanningService.findByStationAndMonth(stationId, month)).thenReturn(shiftPlanningList);

      // Act and Assert
      // Expecting an exception to be thrown
      assertThrows(ServiceValidationException.class, () -> shiftPlanningService.deleteShiftPlanning(stationId, month));

      // Verify that executionDao.deleteForPlannings was not called
      verify(executionDao, never()).deleteForPlannings(any());
   }

   @Test
   void testDeleteShiftPlanning_WhenNoShiftPlannings() {
      // Arrange
      stationId = 1L;
      month = LocalDate.of(2023, 10, 1);

      // Mock finding no shift planning
      List<ShiftPlanningDto> shiftPlanningList = Collections.emptyList();
      when(shiftPlanningService.findByStationAndMonth(stationId, month)).thenReturn(shiftPlanningList);

      // Act
      shiftPlanningService.deleteShiftPlanning(stationId, month);

      // Assert
      verify(executionDao, never()).deleteForPlannings(any()); // No executions should be called
   }

   @Test
   void testResetShiftPlanning_EmptyList() {
      // Arrange
      stationId = 1L;
      month = LocalDate.of(2023, 10, 1);
      request = mock(HttpServletRequest.class);

      // Mock behavior for an empty list
      when(shiftPlanningService.findByStationAndMonth(stationId, month)).thenReturn(List.of());

      // Act
      List<ShiftPlanningDto> result = shiftPlanningService.resetShiftPlanning(stationId, month, request);

      // Assert
      assertTrue(result.isEmpty()); // Should return an empty list
      verify(executionDao, never()).deleteForPlannings(any());
      verify(shiftPlanningDao, never()).deleteAllByIdInBatch(any());
   }
   @Test
   void testFindByStationAndDay() {
      // Arrange
      stationId = 1L;
      LocalDate day = LocalDate.of(2023, 10, 01);
      ShiftPlanningDto planning1 = ShiftPlanningDto.builder().id(1L).build();
      ShiftPlanningDto planning2 = ShiftPlanningDto.builder().id(1L).build();
      List<ShiftPlanningDto> expectedPlanningList = Arrays.asList(planning1, planning2);

      when(shiftPlanningDao.findByStationAndDay(stationId, day)).thenReturn(expectedPlanningList);

      // Act
      List<ShiftPlanningDto> result = shiftPlanningService.findByStationAndDay(stationId, day);

      // Assert
      assertEquals(expectedPlanningList, result);
      verify(shiftPlanningDao, times(1)).findByStationAndDay(stationId, day);
   }

   @Test
   void testFindTeamsByStationAndShiftRotation() {
      // Arrange
      stationId = 1L;
      Long shiftRotationId = 2L;

      // Mocking the returned ids
      List<Long> teamIds = Arrays.asList(1L, 2L);
      when(shiftPlanningDao.findTeamIdsByStationAndShiftRotation(stationId, shiftRotationId)).thenReturn(teamIds);

      // Mocking the service to return DTOs
      PumpAttendantTeamDto team1 = PumpAttendantTeamDto.builder().id(1L).build();
      PumpAttendantTeamDto team2 = PumpAttendantTeamDto.builder().id(2L).build();
      List<PumpAttendantTeamDto> expectedTeams = Arrays.asList(team1, team2);
      when(pumpAttendantTeamService.findByIdsIn(teamIds)).thenReturn(expectedTeams);

      // Act
      List<PumpAttendantTeamDto> result = shiftPlanningService.findTeamsByStationAndShiftRotation(stationId, shiftRotationId);

      // Assert
      assertEquals(expectedTeams, result);
      verify(shiftPlanningDao, times(1)).findTeamIdsByStationAndShiftRotation(stationId, shiftRotationId);
      verify(pumpAttendantTeamService, times(1)).findByIdsIn(teamIds);
   }

   @Test
   void testUpdateList() {
      // Arrange
      List<ShiftPlanningDto> updateList = Arrays.asList(ShiftPlanningDto.builder().build(), ShiftPlanningDto.builder().build());

      // Mocking the DAO to return the updated list
      when(shiftPlanningDao.updateList(updateList)).thenReturn(updateList);

      // Act
      List<ShiftPlanningDto> result = shiftPlanningService.updateList(updateList);

      // Assert
      assertEquals(updateList, result);
      verify(shiftPlanningDao, times(1)).updateList(updateList);
   }

   @Test
   void testMarkAsExecuting() {
      // Arrange
      Long shiftPlanningId = 1L;
      ShiftPlanningDto shiftPlanning = ShiftPlanningDto.builder().build();
      shiftPlanning.setId(shiftPlanningId);
      shiftPlanning.setHasExecution(false);

      when(shiftPlanningDao.findById(shiftPlanningId)).thenReturn(Optional.of(shiftPlanning));

      // Act
      shiftPlanningService.markAsExecuting(shiftPlanningId);

      // Assert
      assertTrue(shiftPlanning.getHasExecution()); // It should now be true
      verify(shiftPlanningDao, times(1)).update(shiftPlanning);
   }
   @Test
   void testBuilderWithPumpAttendantTeam() {
      // Arrange
      ShiftPlanningDto shiftPlanningDto = ShiftPlanningDto.builder().build();
      PumpAttendantTeamDto team = PumpAttendantTeamDto.builder().build();
      team.setName("Team A");

      shiftPlanningDto.setPumpAttendantTeam(team);

      if (shiftPlanningDto.getPumpAttendantTeam() != null) {
         shiftPlanningDto.getPumpAttendantTeam().getName();
      }

      // Assert
      assertNotNull(shiftPlanningDto.getPumpAttendantTeam());
      assertEquals("Team A", shiftPlanningDto.getPumpAttendantTeam().getName());
   }
   @Test
   void testBuilderWithShiftAndShiftRotation() {
      // Arrange
      ShiftPlanningDto shiftPlanningDto = ShiftPlanningDto.builder().build();
      ShiftRotationDto shiftRotation = ShiftRotationDto.builder().build();

      // Mock the Shift object
      ShiftDto shift = ShiftDto.builder().build();
      shift.setOffDay(true);
      shift.setName("Morning Shift");
      shiftRotation.setName("Weekly Rotation");

      shiftPlanningDto.setShift(shift);

      shiftRotation.setName("Daily Rotation");

      shiftPlanningDto.setShiftRotation(shiftRotation);

      // Act
      boolean offDay = false;
      String shiftName = null;
      String shiftRotationName = null;

      if (shiftPlanningDto.getShift() != null) {
         offDay = Objects.nonNull(shiftPlanningDto.getShift().getOffDay()) ? shiftPlanningDto.getShift().getOffDay() : false;
         shiftName = shiftPlanningDto.getShift().getName();
      }
      if(shiftPlanningDto.getShiftRotation() != null) {
         shiftRotationName = shiftPlanningDto.getShiftRotation().getName();
      }

      // Assert
      assertTrue(offDay);
      assertEquals("Morning Shift", shiftName);
      assertEquals("Daily Rotation", shiftRotationName);
   }

   @Test
   void testGeneratePlanning_stationNotFound() {
      // Arrange
      stationId = 1L;
      month = LocalDate.of(2023, 10, 1);
      request = mock(HttpServletRequest.class);

      when(stationDao.findById(stationId)).thenReturn(Optional.empty());

      // Act & Assert
      Exception exception = assertThrows(EntityNotFoundException.class, () -> {
         shiftPlanningService.generatePlanning(stationId, month, request);
      });
      assertEquals("Station not found with ID: " + stationId, exception.getMessage());
   }
}
