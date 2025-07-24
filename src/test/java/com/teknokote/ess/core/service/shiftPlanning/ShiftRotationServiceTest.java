package com.teknokote.ess.core.service.shiftPlanning;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.core.dao.organization.PumpAttendantTeamDao;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningDao;
import com.teknokote.ess.core.dao.shifts.ShiftRotationDao;
import com.teknokote.ess.core.model.movements.EnumFilter;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.impl.StationService;
import com.teknokote.ess.core.service.impl.shifts.ShiftRotationServiceImpl;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningService;
import com.teknokote.ess.dto.PeriodDto;
import com.teknokote.ess.dto.ShiftDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import com.teknokote.ess.http.logger.EntityActionEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShiftRotationServiceTest {
   @InjectMocks
   private ShiftRotationServiceImpl shiftRotationService;
   @Mock
   private StationService stationService;
   @Mock
   private ShiftRotationDao shiftRotationDao;
   @Mock
   private PumpAttendantTeamDao pumpAttendantTeamDao;
   @Mock
   private ShiftPlanningDao shiftPlanningDao;
   @Mock
   private WorkDayShiftPlanningService workDayShiftPlanningService;
   @Mock
   private HttpServletRequest request;
   @Mock
   private SecurityContext securityContext;
   @Mock
   private Authentication authentication;
   @Mock
   private ESSValidationResult validationResult;
   @Mock
   private User mockUser;
   @Mock
   private StationDto stationDto;
   @Mock
   private ESSValidator<ShiftRotationDto> shiftRotationValidator;
   @Mock
   private ApplicationEventPublisher eventPublisher;
   List<ShiftDto> shifts;
   private ShiftRotationDto shiftRotationDto;
   private ShiftRotationDto existingRotationDto;
   private Long stationId = 1L;

   @BeforeEach
   void setUp() {
      MockitoAnnotations.openMocks(this);
      shifts = new ArrayList<>();

      ShiftDto shift1 = ShiftDto.builder()
              .name("Morning Shift")
              .startingTime(LocalTime.of(6, 0))
              .endingTime(LocalTime.of(14, 0))
              .crossesDayBoundary(false)
              .build();

      ShiftDto shift2 = ShiftDto.builder()
              .name("Evening Shift")
              .startingTime(LocalTime.of(14, 0))
              .endingTime(LocalTime.of(22, 0))
              .crossesDayBoundary(false)
              .build();

      shifts.add(shift1);
      shifts.add(shift2);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(mockUser);
      SecurityContextHolder.setContext(securityContext);

      shiftRotationDto = ShiftRotationDto.builder().build();
      // Setting up shiftRotationDto for the new rotation
      shiftRotationDto.setName("New Rotation");
      shiftRotationDto.setStationId(stationId);
      shiftRotationDto.setStartValidityDate(LocalDate.now());
      shiftRotationDto.setEndValidityDate(LocalDate.now().plusDays(7));
      // Assuming starting and ending shifts are set as well
      shiftRotationDto.setShifts(createListOfShifts(LocalTime.of(9, 0), LocalTime.of(17, 0))); // Example shifts
   }

   private List<ShiftDto> createListOfShifts(LocalTime startTime, LocalTime endTime) {
      shifts = new ArrayList<>();
      ShiftDto shift = ShiftDto.builder().build();
      shift.setStartingTime(startTime);
      shift.setEndingTime(endTime);
      shifts.add(shift);
      return shifts;
   }

   @Test
   void addShiftRotation_Success() {
      ShiftRotationDto validShiftRotationDto = ShiftRotationDto.builder()
              .id(1L)
              .shifts(shifts)
              .name("Valid Rotation")
              .stationId(1L)
              .build();

      stationDto = StationDto.builder()
              .id(1L)
              .name("Test Station")
              .customerAccountName("Test Account")
              .build();

      // Mock dependencies
      when(stationService.checkedFindById(1L)).thenReturn(stationDto);
      when(shiftRotationService.getValidator().validateOnCreate(validShiftRotationDto)).thenReturn(validationResult);
      when(shiftRotationService.findAllByStation(1L)).thenReturn(new ArrayList<>());
      when(shiftRotationService.getDao().create(any(ShiftRotationDto.class))).thenReturn(validShiftRotationDto);

      ShiftRotationDto result = shiftRotationService.addShiftRotation(1L, validShiftRotationDto, request);

      assertNotNull(result);
      assertEquals(validShiftRotationDto.getStationId(), result.getStationId());

      ArgumentCaptor<EntityActionEvent> captor = ArgumentCaptor.forClass(EntityActionEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());
   }

   @Test
   void create_ValidShiftRotation_Success() {
      ShiftRotationDto validShiftRotationDto = ShiftRotationDto.builder()
              .id(1L)
              .shifts(shifts)
              .name("Valid Rotation")
              .stationId(1L)
              .build();

      validationResult = new ESSValidationResult();
      when(shiftRotationValidator.validate(validShiftRotationDto)).thenReturn(validationResult);
      when(stationService.checkedFindById(1L)).thenReturn(stationDto);
      when(shiftRotationDao.create(any())).thenReturn(validShiftRotationDto);

      // Call the method under test
      ShiftRotationDto result = shiftRotationService.create(validShiftRotationDto);

      // Verify the result
      assertNotNull(result);
      assertEquals(validShiftRotationDto, result);
   }

   @Test
   void testDeleteShiftRotationWithExecutions() {
      ShiftRotationDto shiftRotation = ShiftRotationDto.builder().build();
      shiftRotation.setId(1L);
      stationId = 1L;

      when(stationService.checkedFindById(stationId)).thenReturn(StationDto.builder().build());
      when(shiftRotationDao.findById(1L)).thenReturn(Optional.of(shiftRotation));
      when(shiftPlanningDao.findByStationAndRotation(stationId, 1L)).thenReturn(new ArrayList<>());

      // Test that when deleting a rotation with executions, an exception is thrown
      when(workDayShiftPlanningService.hasExecutionsForRotation(shiftRotation.getId())).thenReturn(true);

      ServiceValidationException exception = assertThrows(ServiceValidationException.class,
              () -> shiftRotationService.deleteShiftRotation(stationId, 1L, request)
      );

      assertEquals("La suppression de la rotation n'est pas autorisée. Des exécutions ont eu lieu pendant la période définie de cette rotation. Veuillez vérifier et réessayer", exception.getMessage());
   }

   @Test
   void testDeleteShiftRotation() {
      stationId = 1L;
      Long shiftRotationId = 1L;

      shiftRotationDto = ShiftRotationDto.builder().build();
      shiftRotationDto.setId(shiftRotationId);

      when(stationService.checkedFindById(stationId)).thenReturn(StationDto.builder().build());
      when(shiftRotationDao.findById(shiftRotationId)).thenReturn(Optional.of(shiftRotationDto));
      when(workDayShiftPlanningService.hasExecutionsForRotation(shiftRotationId)).thenReturn(false);

      shiftRotationService.deleteShiftRotation(stationId, shiftRotationId, request);

      verify(workDayShiftPlanningService).deleteByStationAndRotation(stationId, shiftRotationId);
      verify(pumpAttendantTeamDao).deleteForStationAndRotation(stationId, shiftRotationId);
      verify(shiftRotationDao).deleteById(shiftRotationId);
      // Verify that the event was published
      verify(eventPublisher).publishEvent(any());
   }

   @Test
   void testAddShiftRotation_OverlapWithExistingRotation() {

      existingRotationDto = ShiftRotationDto.builder().build();
      existingRotationDto.setName("Existing Rotation");
      existingRotationDto.setStationId(stationId);
      existingRotationDto.setStartValidityDate(LocalDate.now());
      existingRotationDto.setEndValidityDate(LocalDate.now().plusDays(7));
      existingRotationDto.setShifts(createListOfShifts(LocalTime.of(8, 0), LocalTime.of(10, 0))); // Example shifts overlapping

      // Mocking the behavior
      when(stationService.checkedFindById(stationId)).thenReturn(StationDto.builder().build()); // Mock the station check
      List<ShiftRotationDto> existingShiftRotations = List.of(existingRotationDto);
      when(shiftRotationService.findAllByStation(stationId)).thenReturn(existingShiftRotations);

      // Expecting ServiceValidationException to be thrown
      Exception exception = assertThrows(ServiceValidationException.class, () -> {
         shiftRotationService.addShiftRotation(stationId, shiftRotationDto, request);
      });

      assertEquals("Overlap with existing shift rotation" + existingRotationDto.getName(), exception.getMessage());
   }

   @Test
   void testShiftOverlap_CrossDayBoundaryWithOverlap() {
      // Create shifts that will overlap
      ShiftDto firstShift = ShiftDto.builder().build();
      firstShift.setName("First Shift");
      firstShift.setStartingTime(LocalTime.of(22, 0)); // Starting at 10 PM
      firstShift.setEndingTime(LocalTime.of(2, 0)); // Ending at 2 AM (crosses day boundary)
      firstShift.setCrossesDayBoundary(true); // It crosses the day boundary

      ShiftDto secondShift = ShiftDto.builder().build();
      secondShift.setName("Second Shift");
      secondShift.setStartingTime(LocalTime.of(1, 0)); // Starting at 1 AM
      secondShift.setEndingTime(LocalTime.of(3, 0)); // Ending at 3 AM
      secondShift.setCrossesDayBoundary(true); // It also crosses the day boundary

      shiftRotationDto = ShiftRotationDto.builder().build();
      shifts = new ArrayList<>();
      shifts.add(firstShift);
      shifts.add(secondShift);
      shiftRotationDto.setShifts(shifts);

      // This should throw a ServiceValidationException due to overlap
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         shiftRotationService.shiftOverlap(shiftRotationDto);
      });

      // Verify exception message
      assertEquals("Overlap between shift " + firstShift.getName() + " and " + secondShift.getName(), exception.getMessage());
   }

   @Test
   void testListPeriodTypesForStation() {
      ShiftRotationDto rotation = ShiftRotationDto.builder().build();
      rotation.setStationId(1L);
      rotation.setStartValidityDate(LocalDate.now());
      rotation.setEndValidityDate(LocalDate.now().plusDays(1));
      ShiftDto shift = ShiftDto.builder().build();
      shift.setStartingTime(LocalTime.of(8, 0));
      shift.setEndingTime(LocalTime.of(16, 0));
      rotation.setShifts(Collections.singletonList(shift));

      when(stationService.checkedFindById(1L)).thenReturn(StationDto.builder().build());
      when(shiftRotationDao.findValidRotation(1L, LocalDate.now())).thenReturn(rotation);

      List<PeriodDto> periods = shiftRotationService.listPeriodTypesForStation(1L);
      assertEquals(5, periods.size());
   }

   @Test
   void testShiftTimeValidationSuccess() {
      shiftRotationDto = ShiftRotationDto.builder().build();
      shiftRotationDto.setShifts(Arrays.asList(
              createShift("Shift A", LocalTime.of(9, 0), LocalTime.of(17, 0)),
              createShift("Shift B", LocalTime.of(18, 0), LocalTime.of(22, 0))
      ));

      // Perform validation
      shiftRotationService.shiftTimeValidation(shiftRotationDto);

      assertNotNull(shiftRotationDto.getShifts());
      assertEquals(2, shiftRotationDto.getShifts().size());
   }

   @Test
   void testShiftTimeValidationInvalidTiming() {
      shiftRotationDto = ShiftRotationDto.builder().build();
      shiftRotationDto.setShifts(Arrays.asList(createShift("Shift A", LocalTime.of(9, 0), LocalTime.of(8, 0))));

      ServiceValidationException exception = assertThrows(ServiceValidationException.class,
              () -> shiftRotationService.shiftTimeValidation(shiftRotationDto));

      assertEquals("Invalid shift timing on shift Shift A: startingTime must be before endingTime", exception.getMessage());
   }

   private ShiftDto createShift(String name, LocalTime startTime, LocalTime endTime) {
      ShiftDto shiftDto = ShiftDto.builder().build();
      shiftDto.setName(name);
      shiftDto.setStartingTime(startTime);
      shiftDto.setEndingTime(endTime);
      return shiftDto;
   }

   @Test
   void whenNoRotationDefinedThenPeriodsStartingOnThisDay() {
      stationService = mock(StationService.class);
      shiftRotationDao = mock(ShiftRotationDao.class);
      when(stationService.checkedFindById(any())).thenReturn(null);
      when(shiftRotationDao.findValidRotation(any(), any())).thenReturn(null);
      shiftRotationService = new ShiftRotationServiceImpl();
      shiftRotationService.setDao(shiftRotationDao);
      shiftRotationService.setStationService(stationService);
      final List<PeriodDto> periods = shiftRotationService.listPeriodTypesForStation(1L);

      assertEquals(5, periods.size());
   }

   @Test
   void withRotationDefinedThenPeriodsStartingOnRotationDay() {
      stationService = mock(StationService.class);
      shiftRotationDao = mock(ShiftRotationDao.class);
      when(stationService.checkedFindById(any())).thenReturn(null);
      shifts = List.of(ShiftDto.builder().startingTime(LocalTime.of(6, 0, 0)).endingTime(LocalTime.of(14, 0, 0)).build(),
              ShiftDto.builder().startingTime(LocalTime.of(14, 0, 0)).endingTime(LocalTime.of(22, 0, 0)).build());
      ShiftRotationDto shiftRotation = ShiftRotationDto.builder().shifts(shifts).build();
      when(shiftRotationDao.findValidRotation(any(), any())).thenReturn(shiftRotation);
      shiftRotationService = new ShiftRotationServiceImpl();
      shiftRotationService.setDao(shiftRotationDao);
      shiftRotationService.setStationService(stationService);
      final List<PeriodDto> periods = shiftRotationService.listPeriodTypesForStation(1L);

      assertEquals(5, periods.size());
      assertValues(periods, 6, 22);
   }

   @Test
   void withRotationDefinedCrossTwoDaysThenPeriodsStartingOnRotationDay() {
      stationService = mock(StationService.class);
      shiftRotationDao = mock(ShiftRotationDao.class);
      when(stationService.checkedFindById(any())).thenReturn(null);
      shifts = List.of(ShiftDto.builder().startingTime(LocalTime.of(6, 0, 0)).endingTime(LocalTime.of(14, 0, 0)).crossesDayBoundary(false).build(),
              ShiftDto.builder().startingTime(LocalTime.of(14, 0, 0)).endingTime(LocalTime.of(22, 0, 0)).crossesDayBoundary(false).build(),
              ShiftDto.builder().startingTime(LocalTime.of(22, 0, 0)).endingTime(LocalTime.of(5, 59, 59)).crossesDayBoundary(true).build()
      );
      ShiftRotationDto shiftRotation = ShiftRotationDto.builder().shifts(shifts).build();
      when(shiftRotationDao.findValidRotation(any(), any())).thenReturn(shiftRotation);
      shiftRotationService = new ShiftRotationServiceImpl();
      shiftRotationService.setDao(shiftRotationDao);
      shiftRotationService.setStationService(stationService);
      final List<PeriodDto> periods = shiftRotationService.listPeriodTypesForStation(1L);

      assertEquals(5, periods.size());
      assertValues(periods, 6, 5);
   }

   private static void assertValues(List<PeriodDto> periods, int start, int end) {
      final PeriodDto resultToday = periods.stream().filter(el -> el.getPeriodType().equals(EnumFilter.TODAY)).findFirst().orElseThrow(() -> new RuntimeException("Inexistant period."));
      final PeriodDto resultYesterday = periods.stream().filter(el -> el.getPeriodType().equals(EnumFilter.YESTERDAY)).findFirst().orElseThrow(() -> new RuntimeException("Inexistant period."));
      final PeriodDto resultWeek = periods.stream().filter(el -> el.getPeriodType().equals(EnumFilter.WEEKLY)).findFirst().orElseThrow(() -> new RuntimeException("Inexistant period."));
      final PeriodDto resultMonth = periods.stream().filter(el -> el.getPeriodType().equals(EnumFilter.MONTHLY)).findFirst().orElseThrow(() -> new RuntimeException("Inexistant period."));
      final PeriodDto resultYear = periods.stream().filter(el -> el.getPeriodType().equals(EnumFilter.YEARLY)).findFirst().orElseThrow(() -> new RuntimeException("Inexistant period."));
      if (resultToday.getEndDateTime().toLocalTime().equals(LocalDateTime.of(1, 1, 1, 23, 59, 59).toLocalTime()))
         assertEquals(start, resultToday.getStartDateTime().getHour());
      assertEquals(end, resultToday.getEndDateTime().getHour());
      assertEquals(start, resultYesterday.getStartDateTime().getHour());
      assertEquals(end, resultYesterday.getEndDateTime().getHour());
      assertEquals(start, resultWeek.getStartDateTime().getHour());
      assertEquals(end, resultWeek.getEndDateTime().getHour());
      assertEquals(start, resultMonth.getStartDateTime().getHour());
      assertEquals(end, resultYear.getEndDateTime().getHour());
      assertEquals(start, resultYear.getStartDateTime().getHour());
      assertEquals(end, resultYear.getEndDateTime().getHour());
      assertEquals(1, resultYear.getStartDateTime().getMonthValue());
      assertEquals(1, resultYear.getStartDateTime().getDayOfYear());
      if (resultToday.getEndDateTime().toLocalTime().equals(LocalDateTime.of(1, 1, 1, 23, 59, 59).toLocalTime()) || resultToday.getEndDateTime().toLocalDate().equals(resultToday.getStartDateTime().toLocalDate())) {
         assertEquals(12, resultYear.getEndDateTime().getMonthValue());
         assertEquals(31, resultYear.getEndDateTime().getDayOfMonth());
      } else {
         assertEquals(1, resultYear.getEndDateTime().getMonthValue());
         assertEquals(1, resultYear.getEndDateTime().getDayOfMonth());
      }

   }

   @Test
   void testShiftTimeValidation_WithValidShifts_NoException() {
      shiftRotationDto = ShiftRotationDto.builder().build();
      shiftRotationDto.setShifts(Arrays.asList(
              createShift("Shift A", LocalTime.of(9, 0), LocalTime.of(17, 0)),
              createShift("Shift B", LocalTime.of(18, 0), LocalTime.of(22, 0))
      ));

      // Perform validation; this should pass without exceptions
      shiftRotationService.shiftTimeValidation(shiftRotationDto);

      // Assertions to verify no exceptions were thrown and shifts are as expected
      assertNotNull(shiftRotationDto.getShifts());
      assertEquals(2, shiftRotationDto.getShifts().size());

      // Verify that the shifts have the correct starting and ending times
      ShiftDto shiftA = shiftRotationDto.getShifts().get(0);
      assertEquals("Shift A", shiftA.getName());
      assertEquals(LocalTime.of(9, 0), shiftA.getStartingTime());
      assertEquals(LocalTime.of(17, 0), shiftA.getEndingTime());

      ShiftDto shiftB = shiftRotationDto.getShifts().get(1);
      assertEquals("Shift B", shiftB.getName());
      assertEquals(LocalTime.of(18, 0), shiftB.getStartingTime());
      assertEquals(LocalTime.of(22, 0), shiftB.getEndingTime());
   }

   @Test
   void testShiftTimeValidation_WithInvalidStartingAndEndingTime_ThrowsException() {
      shiftRotationDto = ShiftRotationDto.builder().build();
      shiftRotationDto.setShifts(Arrays.asList(
              createShift("Invalid Shift A", LocalTime.of(18, 0), LocalTime.of(9, 0))
      ));

      ServiceValidationException exception = assertThrows(ServiceValidationException.class,
              () -> shiftRotationService.shiftTimeValidation(shiftRotationDto));

      assertEquals("Invalid shift timing on shift Invalid Shift A: startingTime must be before endingTime", exception.getMessage());
   }

   @Test
   void testShiftOverlap_WithOverlappingShifts_ThrowsException() {
      ShiftDto morningShift = ShiftDto.builder()
              .name("Morning Shift")
              .startingTime(LocalTime.of(6, 0))
              .endingTime(LocalTime.of(14, 0))
              .crossesDayBoundary(false)
              .build();

      ShiftDto overlappingShift = ShiftDto.builder()
              .name("Overlapping Shift")
              .startingTime(LocalTime.of(13, 0))
              .endingTime(LocalTime.of(20, 0))
              .crossesDayBoundary(false)
              .build();

      shiftRotationDto = ShiftRotationDto.builder()
              .shifts(Arrays.asList(morningShift, overlappingShift))
              .build();

      ServiceValidationException exception = assertThrows(ServiceValidationException.class,
              () -> shiftRotationService.shiftOverlap(shiftRotationDto));

      assertEquals("Overlap between shift " + morningShift.getName() + " and " + overlappingShift.getName(), exception.getMessage());
   }

   @Test
   void testShiftOverlap_WithNonOverlappingShifts_NoException() {
      ShiftDto morningShift = ShiftDto.builder()
              .name("Morning Shift")
              .startingTime(LocalTime.of(6, 0))
              .endingTime(LocalTime.of(14, 0))
              .crossesDayBoundary(false)
              .build();

      ShiftDto eveningShift = ShiftDto.builder()
              .name("Evening Shift")
              .startingTime(LocalTime.of(14, 0))
              .endingTime(LocalTime.of(22, 0))
              .crossesDayBoundary(false)
              .build();

      shiftRotationDto = ShiftRotationDto.builder()
              .shifts(Arrays.asList(morningShift, eveningShift))
              .build();

      // Should not throw any exceptions
      assertDoesNotThrow(() -> shiftRotationService.shiftOverlap(shiftRotationDto), "Expected no exception to be thrown for non-overlapping shifts");

   }

   @Test
   void create_ShiftRotation_Success() {
      ShiftRotationDto rotationDto = createValidShiftRotationDto();

      // Mocking dependencies
      when(stationService.checkedFindById(rotationDto.getStationId())).thenReturn(StationDto.builder().build());
      when(shiftRotationDao.findAllByStation(rotationDto.getStationId())).thenReturn(Collections.emptyList());
      when(shiftRotationDao.create(rotationDto)).thenReturn(rotationDto);

      validationResult = new ESSValidationResult();
      when(shiftRotationService.getValidator().validate(rotationDto)).thenReturn(validationResult);

      ShiftRotationDto result = shiftRotationService.create(rotationDto);

      assertNotNull(result);
      assertEquals(rotationDto, result);
   }

   @Test
   void create_ShiftRotation_WithOverlap_ThrowsException() {
      ShiftRotationDto rotationDto = createValidShiftRotationDto();
      ShiftRotationDto existingRotation = createValidShiftRotationDto();

      when(stationService.checkedFindById(rotationDto.getStationId())).thenReturn(StationDto.builder().build());
      when(shiftRotationDao.findAllByStation(rotationDto.getStationId())).thenReturn(Collections.singletonList(existingRotation));

      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         shiftRotationService.create(rotationDto);
      });

      assertEquals("Overlap with existing shift rotation" + existingRotation.getName(), exception.getMessage());
   }

   @Test
   void shiftOverlap_WithOverlappingShifts_ThrowsException() {
      ShiftRotationDto rotationDto = ShiftRotationDto.builder().build();
      shifts = Arrays.asList(
              createShift("Morning", LocalTime.of(6, 0), LocalTime.of(14, 0)),
              createShift("Overlapping", LocalTime.of(13, 0), LocalTime.of(20, 0))
      );

      rotationDto.setShifts(shifts);

      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> shiftRotationService.shiftOverlap(rotationDto));

      assertEquals("Overlap between shift Morning and Overlapping", exception.getMessage());
   }

   @Test
   void shiftTimeValidation_WithInvalidTime_ThrowsException() {
      shiftRotationDto = ShiftRotationDto.builder().build();
      shiftRotationDto.setShifts(Collections.singletonList(
              createShift("Invalid Shift", LocalTime.of(18, 0), LocalTime.of(16, 0))
      ));

      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> shiftRotationService.shiftTimeValidation(shiftRotationDto));

      assertEquals("Invalid shift timing on shift Invalid Shift: startingTime must be before endingTime", exception.getMessage());
   }

   @Test
   void listPeriodTypesForStation_WhenNoRotationsDefined_ReturnsExpectedPeriods() {
      stationId = 1L;
      when(stationService.checkedFindById(stationId)).thenReturn(StationDto.builder().build());
      when(shiftRotationDao.findValidRotation(stationId, LocalDate.now())).thenReturn(null); // No rotation found

      List<PeriodDto> periods = shiftRotationService.listPeriodTypesForStation(stationId);

      assertEquals(5, periods.size());
      // Additional assertions on periods can be added here if needed
   }

   private ShiftRotationDto createValidShiftRotationDto() {
      ShiftDto shift1 = createShift("Morning Shift", LocalTime.of(6, 0), LocalTime.of(14, 0));
      ShiftDto shift2 = createShift("Evening Shift", LocalTime.of(14, 0), LocalTime.of(22, 0));

      return ShiftRotationDto.builder()
              .id(1L)
              .shifts(Arrays.asList(shift1, shift2))
              .name("Valid Rotation")
              .stationId(1L)
              .startValidityDate(LocalDate.now())
              .endValidityDate(LocalDate.now().plusDays(1))
              .build();
   }

   @Test
   void testCreateWithOverlapFromExistingRotation() {
      ShiftRotationDto existingRotation = createValidShiftRotationDto();
      existingRotation.setName("Existing Rotation");

      when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
      when(shiftRotationDao.findAllByStation(stationId)).thenReturn(Collections.singletonList(existingRotation));

      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () ->
              shiftRotationService.create(existingRotation)
      );

      assertTrue(exception.getMessage().contains("Overlap with existing shift rotation"));
   }

   @Test
   void testShiftTimeValidation_SingleShift_NoException() {
      shiftRotationDto = ShiftRotationDto.builder()
              .shifts(Collections.singletonList(createShift("Single Shift", LocalTime.of(9, 0), LocalTime.of(17, 0))))
              .build();

      assertDoesNotThrow(() -> shiftRotationService.shiftTimeValidation(shiftRotationDto),
              "Expected no exception to be thrown for valid single shift");
   }

   @Test
   void testShiftTimeValidation_InvalidTimes_ThrowsException() {
      shiftRotationDto = ShiftRotationDto.builder()
              .shifts(Arrays.asList(createShift("Invalid Shift", LocalTime.of(18, 0), LocalTime.of(9, 0))))
              .build();

      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () ->
              shiftRotationService.shiftTimeValidation(shiftRotationDto)
      );

      assertEquals("Invalid shift timing on shift Invalid Shift: startingTime must be before endingTime", exception.getMessage());
   }

   @Test
   void testListPeriodTypesForStation_NoRotationDefined_ReturnsDefaultPeriods() {
      when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
      when(shiftRotationDao.findValidRotation(stationId, LocalDate.now())).thenReturn(null);

      List<PeriodDto> periods = shiftRotationService.listPeriodTypesForStation(stationId);

      // Validating there are 5 periods returned and they should be correctly defined
      assertEquals(5, periods.size());
   }

   @Test
   void testDeleteShiftRotation_NoExecutions_Success() {
      Long shiftRotationId = 1L;
      when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
      ShiftRotationDto rotationToDelete = ShiftRotationDto.builder().id(shiftRotationId).build();
      when(shiftRotationDao.findById(shiftRotationId)).thenReturn(Optional.of(rotationToDelete));
      when(workDayShiftPlanningService.hasExecutionsForRotation(shiftRotationId)).thenReturn(false);

      shiftRotationService.deleteShiftRotation(stationId, shiftRotationId, request);
      verify(shiftRotationDao).deleteById(shiftRotationId);
   }

   @Test
   void addShiftRotation_UsesShiftPlanningDao() {
      // Create a ShiftRotationDto with necessary parameters
      ShiftRotationDto shiftRotationDto1 = ShiftRotationDto.builder()
              .id(1L)
              .shifts(shifts)
              .name("New Rotation")
              .stationId(stationId)
              .build();

      stationDto = StationDto.builder()
              .id(1L)
              .name("Test Station")
              .customerAccountName("Test Account")
              .build();

      // Mocking the behavior of the stationService to return a valid station
      when(stationService.checkedFindById(stationId)).thenReturn(stationDto);

      // Create a mock ESSValidationResult and set that it has no errors
      validationResult = mock(ESSValidationResult.class);
      when(validationResult.hasErrors()).thenReturn(false);
      when(shiftRotationService.getValidator().validateOnCreate(shiftRotationDto1)).thenReturn(validationResult);

      when(shiftRotationService.findAllByStation(stationId)).thenReturn(new ArrayList<>());
      when(shiftRotationDao.create(shiftRotationDto1)).thenReturn(shiftRotationDto);

      // Call the method under test
      ShiftRotationDto result = shiftRotationService.addShiftRotation(stationId, shiftRotationDto1, request);

      // Assert that the result is not null and that create method was called
      assertNotNull(result);
      verify(shiftRotationDao).create(any(ShiftRotationDto.class));

      // Additional asserts can be included to check the properties of the result
      assertEquals(shiftRotationDto1.getName(), result.getName());
   }

   @Test
   void deleteShiftRotation_UsesWorkDayShiftPlanningService() {
      ShiftRotationDto shiftRotation = ShiftRotationDto.builder().id(1L).build();
      when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
      when(shiftRotationDao.findById(1L)).thenReturn(Optional.of(shiftRotation));
      when(workDayShiftPlanningService.hasExecutionsForRotation(1L)).thenReturn(false);

      shiftRotationService.deleteShiftRotation(stationId, 1L, request);

      verify(workDayShiftPlanningService).deleteByStationAndRotation(stationId, 1L);
   }

   @Test
   void deleteShiftRotation_UsesPumpAttendantTeamDao() {
      ShiftRotationDto shiftRotation = ShiftRotationDto.builder().id(1L).build();
      when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
      when(shiftRotationDao.findById(1L)).thenReturn(Optional.of(shiftRotation));
      when(workDayShiftPlanningService.hasExecutionsForRotation(1L)).thenReturn(false);

      shiftRotationService.deleteShiftRotation(stationId, 1L, request);

      verify(pumpAttendantTeamDao).deleteForStationAndRotation(stationId, 1L);
   }

   @Test
   void createShiftRotation_UsesEventPublisher() {
      shiftRotationDto = createValidShiftRotationDto();
      when(stationService.checkedFindById(1L)).thenReturn(stationDto);

      // Create a mock ESSValidationResult and specify it has no errors
      validationResult = mock(ESSValidationResult.class);
      when(validationResult.hasErrors()).thenReturn(false);
      when(shiftRotationService.getValidator().validateOnCreate(shiftRotationDto)).thenReturn(validationResult);

      when(shiftRotationService.findAllByStation(1L)).thenReturn(new ArrayList<>());
      when(shiftRotationService.getDao().create(shiftRotationDto)).thenReturn(shiftRotationDto);

      shiftRotationService.addShiftRotation(1L, shiftRotationDto, request);

      verify(eventPublisher).publishEvent(any(EntityActionEvent.class));
   }

   @Test
   void testShiftOverlap_CrossDayBoundary_ThrowsException() {
      // Create the first shift
      ShiftDto shift1 = ShiftDto.builder()
              .name("Night Shift")
              .startingTime(LocalTime.of(22, 0)) // Starts at 10 PM
              .endingTime(LocalTime.of(2, 0))    // Ends at 2 AM
              .crossesDayBoundary(true)           // Crosses day boundary
              .build();

      // Create the second shift
      ShiftDto shift2 = ShiftDto.builder()
              .name("Morning Shift")
              .startingTime(LocalTime.of(6, 0))  // Starts at 6 AM
              .endingTime(LocalTime.of(14, 0))   // Ends at 2 PM
              .crossesDayBoundary(false)          // Does not cross day boundary
              .build();

      // Create the third shift that should overlap with the first shift
      ShiftDto shift3 = ShiftDto.builder()
              .name("Overlapping Night Shift")
              .startingTime(LocalTime.of(1, 0))  // Starts at 1 AM
              .endingTime(LocalTime.of(3, 0))    // Ends at 3 AM
              .crossesDayBoundary(true)           // Crosses day boundary
              .build();

      // Create and set up the ShiftRotationDto with shifts
      shiftRotationDto = ShiftRotationDto.builder()
              .shifts(Arrays.asList(shift1, shift2, shift3))
              .build();

      // Expecting ServiceValidationException to be thrown
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         shiftRotationService.shiftOverlap(shiftRotationDto);
      });

      // Verify the exception message is correct
      assertEquals("Overlap between shift " + shift1.getName() + " and " + shift3.getName(), exception.getMessage());
   }

   @Test
   void testShiftTimeValidation_CrossDayBoundary_InvalidTiming_ThrowsException() {
      // Create a shift that crosses the day boundary with invalid timing
      ShiftDto invalidShift = ShiftDto.builder()
              .name("Invalid Night Shift")
              .startingTime(LocalTime.of(22, 0))  // Starts at 10 PM
              .endingTime(LocalTime.of(23, 0))     // Ends at 11 PM
              .crossesDayBoundary(true)             // Indicates it crosses the day boundary
              .build();

      // Create a ShiftRotationDto with the invalid shift
      shiftRotationDto = ShiftRotationDto.builder()
              .shifts(Arrays.asList(invalidShift)) // Include the invalid shift
              .build();

      // Expecting ServiceValidationException to be thrown
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         shiftRotationService.shiftTimeValidation(shiftRotationDto);
      });

      // Verify the exception message is correct
      assertEquals("Invalid shift timing on shift " + invalidShift.getName() + ": shift must be less than 24 hours", exception.getMessage());
   }

   private ShiftRotationDto createShiftRotation(LocalDate start, LocalDate end) {
      return ShiftRotationDto.builder()
              .startValidityDate(start)
              .endValidityDate(end)
              .build();
   }

   @Test
   void testOverlap_OverlappingDates() {
      ShiftRotationDto newRotation = createShiftRotation(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 5));
      ShiftRotationDto existingRotation = createShiftRotation(LocalDate.of(2023, 10, 3), LocalDate.of(2023, 10, 7));

      assertTrue(shiftRotationService.isOverlap(newRotation, existingRotation));
   }

   @Test
   void testOverlap_NonOverlappingDates() {
      ShiftRotationDto newRotation = createShiftRotation(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 2));
      ShiftRotationDto existingRotation = createShiftRotation(LocalDate.of(2023, 10, 3), LocalDate.of(2023, 10, 4));

      assertFalse(shiftRotationService.isOverlap(newRotation, existingRotation));
   }

   @Test
   void testOverlap_PartialOverlap() {
      ShiftRotationDto newRotation = createShiftRotation(LocalDate.of(2023, 10, 2), LocalDate.of(2023, 10, 6));
      ShiftRotationDto existingRotation = createShiftRotation(LocalDate.of(2023, 10, 3), LocalDate.of(2023, 10, 5));

      assertTrue(shiftRotationService.isOverlap(newRotation, existingRotation));
   }

   @Test
   void testOverlap_IdenticalDates() {
      ShiftRotationDto newRotation = createShiftRotation(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 5));
      ShiftRotationDto existingRotation = createShiftRotation(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 5));

      assertTrue(shiftRotationService.isOverlap(newRotation, existingRotation));
   }

   @Test
   void testOverlapWithExistingRotation() {
      // Setup
      ShiftRotationDto existingRotation = ShiftRotationDto.builder()
              .name("Existing Rotation")
              .stationId(stationId)
              .startValidityDate(LocalDate.now())
              .endValidityDate(LocalDate.now().plusDays(7))
              .shifts(createListOfShifts(LocalTime.of(8, 0), LocalTime.of(10, 0))) // Existing overlapping shift
              .build();

      // Mock behavior for station and existing rotations
      when(stationService.checkedFindById(stationId)).thenReturn(StationDto.builder().build());
      when(shiftRotationService.findAllByStation(stationId)).thenReturn(Collections.singletonList(existingRotation));

      // Attempt to create a new shift rotation with overlapping shifts
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         shiftRotationService.create(shiftRotationDto);
      });

      assertEquals("Overlap with existing shift rotation" + existingRotation.getName(), exception.getMessage());
   }
   @Test
   void testShiftOverlap_WithMultipleShiftsCrossingDayBoundary_ThrowsException() {
      // Overlapping shifts across a day boundary
      ShiftDto nightShift = ShiftDto.builder()
              .name("Night Shift")
              .startingTime(LocalTime.of(22, 0)) // Starts at 10 PM
              .endingTime(LocalTime.of(2, 0))    // Ends at 2 AM (crosses to next day)
              .crossesDayBoundary(true)
              .build();

      ShiftDto earlyMorningShift = ShiftDto.builder()
              .name("Early Morning Shift")
              .startingTime(LocalTime.of(1, 0))  // Starts at 1 AM
              .endingTime(LocalTime.of(3, 0))    // Ends at 3 AM
              .crossesDayBoundary(true)
              .build();

      ShiftDto morningShift = ShiftDto.builder()
              .name("Morning Shift")
              .startingTime(LocalTime.of(6, 0))  // Starts at 6 AM
              .endingTime(LocalTime.of(14, 0))   // Ends at 2 PM
              .crossesDayBoundary(false)
              .build();

      // Set up the ShiftRotationDto with overlapping shifts
      shiftRotationDto = ShiftRotationDto.builder()
              .shifts(Arrays.asList(nightShift, earlyMorningShift, morningShift))
              .build();

      // Expecting ServiceValidationException to be thrown due to overlaps
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         shiftRotationService.shiftOverlap(shiftRotationDto);
      });

      assertEquals("Overlap between shift " + nightShift.getName() + " and " + earlyMorningShift.getName(), exception.getMessage());
   }

   @Test
   void testShiftOverlap_NoShifts_ThrowsException() {
      // Create a ShiftRotationDto without any shifts
      shiftRotationDto = ShiftRotationDto.builder()
              .shifts(new ArrayList<>())
              .build();

      // Expect not to throw but handle gracefully if no shifts to validate
      assertDoesNotThrow(() -> shiftRotationService.shiftOverlap(shiftRotationDto));
   }

   @Test
   void testCreate_OverlapWithExistingRotationWithDifferentStationId_ThrowsException() {
      // Create existing rotation to represent an overlap scenario
      ShiftRotationDto existingRotation = ShiftRotationDto.builder()
              .name("Existing Rotation")
              .stationId(2L) // Different station
              .startValidityDate(LocalDate.now())
              .endValidityDate(LocalDate.now().plusDays(5))
              .shifts(Arrays.asList(createShift("Shift1", LocalTime.of(9, 0), LocalTime.of(17, 0))))
              .build();

      // Create new rotation that overlaps with the existing one
      ShiftRotationDto newRotation = ShiftRotationDto.builder()
              .name("New Rotation")
              .stationId(1L) // New station ID
              .startValidityDate(LocalDate.now())
              .endValidityDate(LocalDate.now().plusDays(5))
              .shifts(Arrays.asList(createShift("Shift2", LocalTime.of(8, 0), LocalTime.of(16, 0))))
              .build();

      when(stationService.checkedFindById(1L)).thenReturn(mock(StationDto.class));
      when(shiftRotationDao.findAllByStation(1L)).thenReturn(Collections.singletonList(existingRotation));

      // Expecting ServiceValidationException to be thrown
      ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
         shiftRotationService.create(newRotation);
      });

      assertEquals("Overlap with existing shift rotation" + existingRotation.getName(), exception.getMessage());
   }
}

