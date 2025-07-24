package com.teknokote.ess.core.service.shiftPlanning;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.ess.core.dao.shifts.PumpAttendantCollectionSheetDao;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningExecutionDao;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningExecutionDetailDao;
import com.teknokote.ess.core.dao.shifts.WorkDayShiftPlanningExecutionDao;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Nozzle;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.core.model.organization.EnumAffectationMode;
import com.teknokote.ess.core.model.shifts.ShiftExecutionStatus;
import com.teknokote.ess.core.service.PumpAttendantService;
import com.teknokote.ess.core.service.impl.ControllerPtsConfigurationService;
import com.teknokote.ess.core.service.impl.PumpService;
import com.teknokote.ess.core.service.impl.StationService;
import com.teknokote.ess.core.service.impl.shifts.ShiftPlanningExecutionServiceImpl;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.core.service.impl.validators.shifts.ShiftDetailUpdatesValidator;
import com.teknokote.ess.core.service.impl.validators.shifts.ShiftPlanningExecutionValidator;
import com.teknokote.ess.core.service.mappers.ShiftPlanningExecutionDtoMapper;
import com.teknokote.ess.core.service.shifts.PaymentMethodService;
import com.teknokote.ess.core.service.shifts.PumpAttendantCollectionSheetService;
import com.teknokote.ess.core.service.shifts.ShiftPlanningService;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningService;
import com.teknokote.ess.dto.ControllerPtsDto;
import com.teknokote.ess.dto.PumpAttendantDto;
import com.teknokote.ess.dto.ShiftDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningExecutionDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import com.teknokote.ess.dto.shifts.*;
import com.teknokote.ess.exceptions.ShiftExecutionException;
import com.teknokote.ess.http.logger.EntityActionEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ShiftPlanningExecutionServiceImplTest {
    @InjectMocks
    @Spy
    private ShiftPlanningExecutionServiceImpl shiftPlanningExecutionService;
    @Mock
    private PumpAttendantService pumpAttendantService;
    @Mock
    private PumpAttendantCollectionSheetService pumpAttendantCollectionSheetService;
    @Mock
    private ShiftPlanningExecutionValidator shiftPlanningExecutionValidator;
    @Mock
    private WorkDayShiftPlanningService workDayShiftPlanningService;
    @Mock
    private PaymentMethodService paymentMethodService;
    @Mock
    private WorkDayShiftPlanningExecutionDao workDayShiftPlanningExecutionDao;
    @Mock
    private PumpService pumpService;
    @Mock
    private StationService stationService;
    @Mock
    private ShiftPlanningExecutionDao shiftPlanningExecutionDao;
    @Mock
    private PumpAttendantCollectionSheetDao pumpAttendantCollectionSheetDao;
    @Mock
    private ControllerPtsConfigurationService controllerPtsConfigurationService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ShiftPlanningService shiftPlanningService;
    @Mock
    private ShiftDetailUpdatesValidator shiftDetailUpdatesValidator;
    @Mock
    TransactionService transactionService;
    @Mock
    private ShiftPlanningExecutionDtoMapper shiftPlanningExecutionDtoMapper;
    @Mock
    private HttpServletRequest request;
    private ShiftPlanningExecutionDto shiftPlanningExecutionDto;
    private AffectedPumpAttendantDto affectedPumpAttendantDto;
    @Mock
    private WorkDayShiftPlanningDto workDayShiftPlanningDto;
    @Mock
    private ShiftPlanningExecutionDetailDao detailDao;
    private ShiftPlanningDto shiftPlanningDto;
    private StationDto stationDto;
    private ControllerPtsDto controllerPtsDto;
    private ShiftDto shiftDto;
    private WorkDayShiftPlanningExecutionDto workDayShiftPlanningExecutionDto;
    private ShiftPlanningExecutionDetailDto originalShiftExecutionDetailToUpdate;
    private ShiftDetailUpdatesDto shiftDetailModification;
    @Mock
    private ShiftPlanningExecutionDto shiftPlanningExecution;
    private Long originalPumpAttendantId;
    private Long concernedPumpId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock getValidator() method
        lenient().doReturn(shiftPlanningExecutionValidator).when(shiftPlanningExecutionService).getValidator();

        // Mock validator behavior to avoid NullPointerException
        lenient().when(shiftPlanningExecutionValidator.validateOnUpdate(any()))
                .thenReturn(new ESSValidationResult());

        // Initialize mock data
        affectedPumpAttendantDto = createAffectedPumpAttendantDto();
        workDayShiftPlanningDto = createWorkDayShiftPlanningDto();
        controllerPtsDto = createControllerPtsDto();
        stationDto = createStationDto();
        shiftDto = createShiftDto();
        shiftPlanningDto = createShiftPlanningDto();
        shiftPlanningExecutionDto = createShiftPlanningExecutionDto();

        // Build WorkDayShiftPlanningExecutionDto
        workDayShiftPlanningExecutionDto = WorkDayShiftPlanningExecutionDto.builder().id(1L).build();

        // Initialize PumpAttendantTeamDto and link it to ShiftPlanningDto
        PumpAttendantTeamDto pumpAttendantTeam = PumpAttendantTeamDto.builder()
                .affectedPumpAttendant(new HashSet<>(List.of(affectedPumpAttendantDto)))
                .build();
        shiftPlanningDto.setPumpAttendantTeam(pumpAttendantTeam);

        originalShiftExecutionDetailToUpdate = ShiftPlanningExecutionDetailDto.builder().forced(false).build();
        originalShiftExecutionDetailToUpdate.setTankReturn(BigDecimal.valueOf(100L));

        shiftDetailModification = ShiftDetailUpdatesDto.builder().build();
        shiftDetailModification.setTankReturn(BigDecimal.valueOf(50L));
        shiftDetailModification.setStartDateTime(LocalDateTime.now());
        shiftDetailModification.setEndDateTime(LocalDateTime.now().plusHours(1));
        shiftDetailModification.setPumpAttendantId(2L);

        shiftPlanningExecution = mock(ShiftPlanningExecutionDto.class);
        originalPumpAttendantId = 1L;
        concernedPumpId = 1L;

    }
    @Test
    void testValidateShiftDetailRequestedModifications_WithValidationError() {
        // Arrange
        shiftDetailModification = ShiftDetailUpdatesDto.builder().build();
        ESSValidationResult validationResult = mock(ESSValidationResult.class);

        // Mocking the validator to return a validation error
        when(shiftDetailUpdatesValidator.validate(shiftDetailModification)).thenReturn(validationResult);
        when(validationResult.hasErrors()).thenReturn(true);
        when(validationResult.getMessage()).thenReturn("Validation error occurred.");

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
            shiftPlanningExecutionService.validateShiftDetailRequestedModifications(shiftDetailModification);
        });

        assertEquals("Validation error occurred.", exception.getMessage());
    }
    @Test
    void testValidateShiftDetailRequestedModifications_WithoutValidationError() {
        // Arrange
        shiftDetailModification = ShiftDetailUpdatesDto.builder().build();
        ESSValidationResult validationResult = mock(ESSValidationResult.class);

        // Mocking the validator to return no validation errors
        when(shiftDetailUpdatesValidator.validate(shiftDetailModification)).thenReturn(validationResult);
        when(validationResult.hasErrors()).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> {
            shiftPlanningExecutionService.validateShiftDetailRequestedModifications(shiftDetailModification);
        });
    }
    @Test
    void testCalculateTotalAmountPumpAttendant() {
        // Arrange
        Long shiftPlanningExecutionId = 1L;
        Long pumpAttendantId = 1L;

        // Create dummy data for ShiftPlanningExecutionDetailDto
        ShiftPlanningExecutionDetailDto detail1 = ShiftPlanningExecutionDetailDto.builder()
                .forced(false)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        ShiftPlanningExecutionDetailDto detail2 = ShiftPlanningExecutionDetailDto.builder()
                .forced(false)
                .totalAmount(BigDecimal.valueOf(150.00))
                .build();

        List<ShiftPlanningExecutionDetailDto> mockDetails = Arrays.asList(detail1, detail2);

        // Mocking the behavior of detailDao
        when(detailDao.findByShiftPlanningExecutionIdAndPumpAttendantId(shiftPlanningExecutionId, pumpAttendantId))
                .thenReturn(mockDetails);

        // Act
        BigDecimal totalAmount = shiftPlanningExecutionService.calculateTotalAmountPumpAttendant(shiftPlanningExecutionId, pumpAttendantId);

        // Assert
        assertEquals(BigDecimal.valueOf(250.00), totalAmount, "The total amount should be 250.00");

        // Verify that the DAO method was called correctly
        verify(detailDao).findByShiftPlanningExecutionIdAndPumpAttendantId(shiftPlanningExecutionId, pumpAttendantId);
    }
    @Test
    void testCalculateTotalAmountPumpAttendant_EmptyList() {
        // Arrange
        Long shiftPlanningExecutionId = 1L;
        Long pumpAttendantId = 1L;

        // Mocking the behavior of detailDao to return an empty list
        when(detailDao.findByShiftPlanningExecutionIdAndPumpAttendantId(shiftPlanningExecutionId, pumpAttendantId)).thenReturn(Arrays.asList());

        // Act
        BigDecimal totalAmount = shiftPlanningExecutionService.calculateTotalAmountPumpAttendant(shiftPlanningExecutionId, pumpAttendantId);

        // Assert
        assertEquals(BigDecimal.ZERO, totalAmount, "The total amount should be zero when there are no details");

        // Verify that the DAO method was called correctly
        verify(detailDao).findByShiftPlanningExecutionIdAndPumpAttendantId(shiftPlanningExecutionId, pumpAttendantId);
    }
    @Test
    void testMapNewExecutions_Success() {
        LocalDate today = LocalDate.now();
        controllerPtsDto = ControllerPtsDto.builder()
                .id(1L)
                .ptsId("0027003A3438510935383135")
                .build();

        stationDto = StationDto.builder()
                .id(1L)
                .controllerPts(controllerPtsDto)
                .build();

        when(stationService.checkedFindById(stationDto.getId())).thenReturn(stationDto);
        ESSValidationResult validationResult = new ESSValidationResult();

        doAnswer(invocation -> {
            invocation.getArgument(0);
            return validationResult;
        }).when(shiftPlanningExecutionValidator).validateOnCreate(any());

        workDayShiftPlanningDto = WorkDayShiftPlanningDto.builder()
                .id(1L)
                .day(today)
                .build();

        // Create PumpAttendantTeamDto with affectedPumpAttendant set
        AffectedPumpAttendantDto affectedPumpAttendant = AffectedPumpAttendantDto.builder()
                .id(1L)
                .pumpAttendantId(1L)
                .build();

        PumpAttendantTeamDto pumpAttendantTeamDto1 = PumpAttendantTeamDto.builder()
                .affectedPumpAttendant(new HashSet<>(List.of(affectedPumpAttendant)))
                .build();

        ShiftPlanningDto shiftPlanningDto1 = ShiftPlanningDto.builder()
                .shift(shiftDto)
                .pumpAttendantTeamId(1L)
                .pumpAttendantTeam(pumpAttendantTeamDto1)
                .workDayShiftPlanningId(workDayShiftPlanningDto.getId())
                .stationId(stationDto.getId())
                .build();

        // Create another PumpAttendantTeamDto for shiftPlanningDto2
        AffectedPumpAttendantDto affectedPumpAttendant2 = AffectedPumpAttendantDto.builder()
                .id(2L)
                .pumpAttendantId(2L)
                .build();

        PumpAttendantTeamDto pumpAttendantTeamDto2 = PumpAttendantTeamDto.builder()
                .affectedPumpAttendant(new HashSet<>(List.of(affectedPumpAttendant2)))
                .build();

        ShiftPlanningDto shiftPlanningDto2 = ShiftPlanningDto.builder()
                .shift(shiftDto)
                .pumpAttendantTeamId(2L)
                .pumpAttendantTeam(pumpAttendantTeamDto2)
                .workDayShiftPlanningId(workDayShiftPlanningDto.getId())
                .stationId(stationDto.getId())
                .build();

        // Add ShiftPlanningDto to the Set
        Set<ShiftPlanningDto> shiftPlanningDtos = new HashSet<>(Arrays.asList(shiftPlanningDto1, shiftPlanningDto2));
        workDayShiftPlanningDto.setShiftPlannings(shiftPlanningDtos);

        // Mock the workDayShiftPlanningService's behavior
        WorkDayShiftPlanningDto mockedWorkDayShiftPlanning = WorkDayShiftPlanningDto.builder()
                .id(workDayShiftPlanningDto.getId())
                .day(today)
                .build();

        when(workDayShiftPlanningService.checkedFindById(workDayShiftPlanningDto.getId()))
                .thenReturn(mockedWorkDayShiftPlanning);

        workDayShiftPlanningExecutionDto = WorkDayShiftPlanningExecutionDto.builder()
                .workDayShiftPlanningId(1L)
                .build();

        when(workDayShiftPlanningExecutionDao.create(any())).thenReturn(workDayShiftPlanningExecutionDto);

        DynamicShiftPlanningExecutionDto dynamicDto1 = new DynamicShiftPlanningExecutionDto();
        dynamicDto1.setShiftIndex(1);

        DynamicShiftPlanningExecutionDto dynamicDto2 = new DynamicShiftPlanningExecutionDto();
        dynamicDto2.setShiftIndex(2);

        // Ensure this mock returns appropriately
        when(shiftPlanningExecutionDtoMapper.toDynamic(any())).thenReturn(dynamicDto1, dynamicDto2);

        // Act
        List<DynamicShiftPlanningExecutionDto> result = shiftPlanningExecutionService.mapNewExecutions(workDayShiftPlanningDto);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getShiftIndex());
        assertEquals(2, result.get(1).getShiftIndex());

        verify(workDayShiftPlanningExecutionDao, times(1)).create(any());
    }
    @Test
    void testMapNewExecutions_WhenNullWorkDayShiftPlanning_ShouldThrowException() {
        // Arrange
        WorkDayShiftPlanningDto workDayShiftPlanning = null;

        // Act and Assert
        Exception exception = assertThrows(ServiceValidationException.class, () -> {
            shiftPlanningExecutionService.mapNewExecutions(workDayShiftPlanning);
        });

        assertEquals("No Scheduled generated !! please generate scheduled for current rotation to access the agenda", exception.getMessage());
    }
    @Test
    void testMapNewExecutions_WhenShiftsAreUnaffected_ShouldThrowException() {
        // Arrange
        workDayShiftPlanningDto = WorkDayShiftPlanningDto.builder().id(1L).build();

        shiftPlanningDto = ShiftPlanningDto.builder().shift(shiftDto).pumpAttendantTeamId(null).build();

        Set<ShiftPlanningDto> shiftPlanningDtos = new HashSet<>(Arrays.asList(shiftPlanningDto));
        workDayShiftPlanningDto.setShiftPlannings(shiftPlanningDtos);

        // Act and Assert
        Exception exception = assertThrows(ServiceValidationException.class, () -> {
            shiftPlanningExecutionService.mapNewExecutions(workDayShiftPlanningDto);
        });

        assertEquals("One or more shifts are unaffected to teams!", exception.getMessage());
    }
    @Test
    void testHandlePumpAttendantChange_LockedShift() {
        // Arrange
        when(shiftPlanningExecution.isLocked()).thenReturn(true);

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
            ShiftPlanningExecutionServiceImpl.handlePumpAttendantChange(originalShiftExecutionDetailToUpdate, shiftDetailModification, shiftPlanningExecution, originalPumpAttendantId, concernedPumpId);
        });

        assertEquals("Modification n'est pas disponible pour un poste verouillé", exception.getMessage());
    }
    @Test
    void testHandlePumpAttendantChange_ReinforcementRequested() {
        // Arrange
        when(shiftPlanningExecution.isLocked()).thenReturn(false);

        // Build the ShiftDetailUpdatesDto with reinforcement requested
        shiftDetailModification = ShiftDetailUpdatesDto.builder()
                .id(1L)
                .shiftPlanningExecutionId(1L)
                .pumpAttendantId(2L)
                .startDateTime(LocalDateTime.now().minusHours(1))
                .endDateTime(LocalDateTime.now())
                .tankReturn(BigDecimal.valueOf(50))
                .reinforcementRequested(true)
                .build();

        // Create a mock list of ShiftPlanningExecutionDetailDto
        List<ShiftPlanningExecutionDetailDto> mockDetails = new ArrayList<>();
        ShiftPlanningExecutionDetailDto detail = ShiftPlanningExecutionDetailDto.builder()
                .startDateTime(LocalDateTime.now().minusHours(1))
                .endDateTime(LocalDateTime.now())
                .forced(false)
                .build();
        mockDetails.add(detail);
        // Stub the method to return a stream from the mock list
        when(shiftPlanningExecution.getPumpAttendantShiftExecutionDetails(originalPumpAttendantId))
                .thenReturn(mockDetails.stream());

        // Act
        shiftPlanningExecutionService.handlePumpAttendantChange(originalShiftExecutionDetailToUpdate, shiftDetailModification, shiftPlanningExecution, originalPumpAttendantId, concernedPumpId);

        // Assert
        verify(shiftPlanningExecution).addShiftPlanningExecutionDetails(anyList());
    }
    @Test
    void testUpdateShiftDetails_Success() {
        // Arrange
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 1L;
        Long shiftDetailId = 1L;

        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(shiftPlanningExecutionValidator.validateOnUpdate(any()))
                .thenReturn(new ESSValidationResult());

        ESSValidationResult shiftDetailValidationResult = new ESSValidationResult();
        when(shiftDetailUpdatesValidator.validate(any(ShiftDetailUpdatesDto.class)))
                .thenReturn(shiftDetailValidationResult);

        ShiftDetailUpdatesDto shiftDetailUpdatesDto = ShiftDetailUpdatesDto.builder()
                .shiftPlanningExecutionId(shiftPlanningExecutionId)
                .id(shiftDetailId)
                .startDateTime(LocalDateTime.now().minusHours(1))
                .endDateTime(LocalDateTime.now())
                .pumpAttendantId(1L)
                .build();

        shiftDetailUpdatesDto.setStationId(1L);
        ShiftPlanningExecutionDetailDto existingDetail = ShiftPlanningExecutionDetailDto.builder()
                .id(shiftDetailId)
                .pumpId(1L)
                .pumpAttendantId(2L)
                .forced(false)
                .build();

        ShiftPlanningExecutionDto existingShiftPlanningExecution = ShiftPlanningExecutionDto.builder()
                .id(shiftPlanningExecutionId)
                .shift(ShiftDto.builder().name("Test Shift").crossesDayBoundary(false).build())
                .shiftPlanningExecutionDetail(Collections.singletonList(existingDetail))
                .build();

        stationDto = StationDto.builder().id(stationId).name("Test Station").build();

        // Mocking behaviors
        when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
        when(shiftPlanningExecutionDao.findByStation(shiftPlanningExecutionId, stationId)).thenReturn(Optional.of(existingShiftPlanningExecution));

        // Mocking the update behavior
        when(shiftPlanningExecutionService.update(existingShiftPlanningExecution)).thenReturn(existingShiftPlanningExecution);

        // Event publishing mock
        doNothing().when(eventPublisher).publishEvent(any(EntityActionEvent.class));

        // Act
        ShiftPlanningExecutionDto result = shiftPlanningExecutionService.updateShiftDetails(shiftDetailUpdatesDto, mock(HttpServletRequest.class));

        // Assert
        assertNotNull(result);
        assertEquals(existingShiftPlanningExecution, result);
        // Verify event publishing
        verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
        // Verify the interaction with the DAO
        verify(shiftPlanningExecutionDao).findByStation(shiftPlanningExecutionId, stationId);
        log.info("Shift details updated successfully for station {}", stationDto.getName());
    }
    @Test
    void updateCollectionSheetForPumpAttendant_Success() {
        // Arrange
        Long shiftPlanningExecutionId = 1L;
        Long pumpAttendantId = 1L;

        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        PumpAttendantCollectionSheetDto collectionSheetDto = PumpAttendantCollectionSheetDto.builder()
                .shiftPlanningExecutionId(shiftPlanningExecutionId)
                .pumpAttendantId(pumpAttendantId)
                .build();

        shiftDto = ShiftDto.builder().id(1L).build();

        // Mock ShiftPlanningExecutionDto
        shiftPlanningExecution = ShiftPlanningExecutionDto.builder()
                .id(shiftPlanningExecutionId)
                .shift(shiftDto)
                .pumpAttendantCollectionSheets(Collections.singletonList(collectionSheetDto))
                .shiftPlanningExecutionDetail(Collections.emptyList())
                .status(ShiftExecutionStatus.COMPLETED)
                .build();

        // Mock the expected behavior
        when(shiftPlanningExecutionDao.findById(shiftPlanningExecutionId)).thenReturn(Optional.ofNullable(shiftPlanningExecution));

        // Mock PumpAttendant data
        PumpAttendantDto pumpAttendantDto = PumpAttendantDto.builder()
                .id(pumpAttendantId)
                .firstName("John")
                .lastName("Doe")
                .build();

        when(pumpAttendantService.checkedFindById(pumpAttendantId)).thenReturn(pumpAttendantDto);

        // Mock the update operation to return the same DTO
        when(pumpAttendantCollectionSheetService.update(any())).thenReturn(collectionSheetDto);

        // Mocking the event publisher
        doNothing().when(eventPublisher).publishEvent(any(EntityActionEvent.class));

        // Set up the servlet request context
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        PumpAttendantCollectionSheetDto result = shiftPlanningExecutionService.updateCollectionSheetForPumpAttendant(collectionSheetDto, request);

        // Assert
        assertNotNull(result);
        assertEquals(collectionSheetDto.getPumpAttendantId(), result.getPumpAttendantId());
        verify(shiftPlanningExecutionService, times(1)).checkedFindById(shiftPlanningExecutionId);
        verify(pumpAttendantService, times(1)).checkedFindById(pumpAttendantId);
        verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
        verify(pumpAttendantCollectionSheetService, times(1)).update(collectionSheetDto);
    }
    @Test
    void testUnlock_NotLocked() {
        // Arrange
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 1L;

        // Setup a ShiftPlanningExecutionDto that is NOT LOCKED
        shiftPlanningExecutionDto = createShiftPlanningExecutionDto();
        shiftPlanningExecutionDto.setStatus(ShiftExecutionStatus.COMPLETED);

        // Mock method behaviors
        when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
        when(shiftPlanningExecutionDao.findById(shiftPlanningExecutionId)).thenReturn(Optional.of(shiftPlanningExecutionDto));

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
            shiftPlanningExecutionService.unlock(stationId, shiftPlanningExecutionId, request);
        });

        assertEquals("Shift planning execution is at status :[COMPLETED] and can't be UNLOCKED !!! ", exception.getMessage());
    }
    @Test
    void testLock_NotCompleted() {
        // Arrange
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 1L;

        shiftPlanningExecutionDto.setStatus(ShiftExecutionStatus.IN_PROGRESS);

        when(shiftPlanningExecutionDao.findById(shiftPlanningExecutionId)).thenReturn(Optional.of(shiftPlanningExecutionDto));

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
            shiftPlanningExecutionService.lock(stationId, shiftPlanningExecutionId, request);
        });

        assertEquals("Shift planning execution is at status :[IN_PROGRESS] and can't be LOCKED !!! ", exception.getMessage());
    }
    @Test
    void testLock_AllSheetsNotTreated() {
        // Arrange
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 1L;

        when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
        when(pumpAttendantCollectionSheetDao.findByShiftPlanningExecutionId(shiftPlanningExecutionId)).thenReturn(new ArrayList<>());
        when(shiftPlanningExecutionDao.findById(shiftPlanningExecutionId)).thenReturn(Optional.of(shiftPlanningExecutionDto));
        when(shiftPlanningExecutionService.allSheetsTreated(shiftPlanningExecutionDto)).thenReturn(false);

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
            shiftPlanningExecutionService.lock(stationId, shiftPlanningExecutionId, request);
        });

        assertEquals("Collection for one or more pumpAttendant are not created, please verify your shift recipe  ", exception.getMessage());
    }
    @Test
    void testGenerateExecutionDetailsPerPumpAttendant() {
        // Arrange
        stationDto = StationDto.builder().build();
        stationDto.setControllerPts(ControllerPtsDto.builder().build());

        ControllerPtsConfiguration controllerPtsConfiguration = new ControllerPtsConfiguration();

        Pump pump = new Pump();
        Nozzle nozzle = new Nozzle();
        nozzle.setId(1L);
        pump.setNozzleList(List.of(nozzle));

        when(stationService.checkedFindById(any())).thenReturn(stationDto);
        when(controllerPtsConfigurationService.findCurrentConfigurationOnController(any())).thenReturn(controllerPtsConfiguration);
        when(pumpService.findByIdConfAndControllerPtsConfiguration(any(), any())).thenReturn(Optional.of(pump));

        // Act
        List<ShiftPlanningExecutionDetailDto> result = shiftPlanningExecutionService.generateExecutionDetailsPerPumpAttendant(
                affectedPumpAttendantDto, workDayShiftPlanningDto, shiftPlanningDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ShiftPlanningExecutionDetailDto detail = result.get(0);
        assertEquals(1L, detail.getPumpAttendantId());
        assertEquals(1L, detail.getPumpId());
        assertEquals(new BigDecimal(0), detail.getStartIndexAmount());
        assertEquals(new BigDecimal(0), detail.getEndIndexAmount());
        assertEquals(nozzle.getId(), detail.getNozzleId());
    }
    @Test
    void testStart_ShiftExecutionSuccessful() {
        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Long stationId = 1L;
        String ptsId = "0027003A3438510935383135";
        Long shiftExecutionId = 1L;
        LocalDateTime startDateTime = LocalDateTime.now();
        shiftDto = ShiftDto.builder().id(1L).name("Jour").build();
        // Create mock ShiftPlanningExecutionDto
        ShiftPlanningExecutionDto shiftExecution = ShiftPlanningExecutionDto.builder()
                .id(shiftExecutionId)
                .shift(shiftDto)
                .status(ShiftExecutionStatus.WAITING)
                .startDateTime(startDateTime.minusHours(1))
                .shiftPlanningExecutionDetail(new ArrayList<>()) // Initialize details
                .build();
        Pump pump = new Pump();
        Nozzle nozzle = new Nozzle();
        // Mocking shift details
        ShiftPlanningExecutionDetailDto shiftDetail = ShiftPlanningExecutionDetailDto.builder()
                .id(1L).pumpId(1L).nozzleId(1L).forced(false).nozzle(nozzle).pump(pump).build();
        shiftDetail.setStartDateTime(startDateTime);

        shiftExecution.getShiftPlanningExecutionDetail().add(shiftDetail);

        // Mock Shift Execution Retrieval
        lenient().when(shiftPlanningExecutionDao.findByStation(shiftExecutionId, stationId)).thenReturn(Optional.of(shiftExecution));
        lenient().when(shiftPlanningExecutionDao.findInProgressExecution(stationId)).thenReturn(null);
        lenient().when(shiftPlanningExecutionDao.findByStationAndDay(stationId, LocalDate.now())).thenReturn(new ArrayList<>());
        controllerPtsDto = ControllerPtsDto.builder().ptsId(ptsId).build();
        // Mock Station Service
        StationDto station = StationDto.builder()
                .id(stationId)
                .controllerPts(controllerPtsDto)
                .modeAffectation(EnumAffectationMode.MANUEL)
                .build();
        lenient().when(stationService.checkedFindById(stationId)).thenReturn(station);

        // Mock Pump Transactions
        lenient().when(transactionService.findFirstTransactionOnDate(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        // Mock Event Publishing
        doNothing().when(eventPublisher).publishEvent(any(EntityActionEvent.class));
        // Run the method
        ShiftPlanningExecutionDto result = shiftPlanningExecutionService.start(stationId, shiftExecutionId, startDateTime, null);
        verify(shiftPlanningExecutionService, times(1)).start(stationId, shiftExecutionId, startDateTime, null);
        // Verify expected result
        assertEquals(ShiftExecutionStatus.IN_PROGRESS, result.getStatus());
        assertEquals(startDateTime, result.getStartDateTime());
        log.info("started shift {}",shiftExecution.getStatus());
    }
    @Test
    void testStart_ShiftAlreadyInProgress() {
        Long stationId = 1L;
        Long shiftExecutionId = 1L;
        LocalDateTime startDateTime = LocalDateTime.now();
        ShiftPlanningExecutionDetailDto shiftPlanningExecutionDetailDto = ShiftPlanningExecutionDetailDto.builder()
                .id(1L).pumpId(1L).nozzleId(1L).forced(false).build();
        ShiftPlanningExecutionDto existingShiftExecution = ShiftPlanningExecutionDto.builder()
                .id(2L)
                .shiftPlanningExecutionDetail(List.of(shiftPlanningExecutionDetailDto))
                .status(ShiftExecutionStatus.IN_PROGRESS)
                .build();
        when(shiftPlanningExecutionDao.findByStation(shiftExecutionId, stationId)).thenReturn(Optional.of(existingShiftExecution));
        lenient().when(shiftPlanningExecutionDao.findInProgressExecution(stationId)).thenReturn(existingShiftExecution);

        ServiceValidationException exception = assertThrows(ServiceValidationException.class,
                () -> shiftPlanningExecutionService.start(stationId, shiftExecutionId, startDateTime, null));

        assertEquals("Shift planning execution is at status :[IN_PROGRESS] and can't be started !!!".trim(),
                exception.getMessage().trim());    }
    @Test
    void testStop_ShiftExecutionSuccessful() {
        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Long stationId = 1L;
        Long shiftExecutionId = 1L;
        String ptsId = "0027003A3438510935383135";
        LocalDateTime stopDateTime = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.now().minusHours(8);
        shiftDto = ShiftDto.builder().id(1L).name("Jour").build();
        Pump pump = new Pump();
        Nozzle nozzle = new Nozzle();
        // Mocking shift details
        ShiftPlanningExecutionDetailDto shiftDetail = ShiftPlanningExecutionDetailDto.builder()
                .id(1L).pumpId(1L).nozzleId(1L).forced(false).nozzle(nozzle)
                .startIndexAmount(BigDecimal.valueOf(25675925)).startIndex(BigDecimal.valueOf(236532)).pump(pump).build();
        shiftDetail.setStartDateTime(startDateTime);

        // Create mock ShiftPlanningExecutionDto
        ShiftPlanningExecutionDto shiftExecution = ShiftPlanningExecutionDto.builder()
                .id(shiftExecutionId)
                .startDateTime(startDateTime)
                .endDateTime(stopDateTime)
                .shift(shiftDto)
                .status(ShiftExecutionStatus.IN_PROGRESS)
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .build();
        shiftExecution.getShiftPlanningExecutionDetail().add(shiftDetail);
        shiftExecution.setPumpAttendantCollectionSheets(new ArrayList<>());
        Map<Long, BigDecimal> pumpAttendantSheetMap = new HashMap<>();
        pumpAttendantSheetMap.put(1L,BigDecimal.valueOf(20000));
        shiftExecution.createPumpAttendantSheets(pumpAttendantSheetMap);
        PaymentMethodDto paymentMethodDto = PaymentMethodDto.builder().id(1L).build();
        controllerPtsDto = ControllerPtsDto.builder().ptsId(ptsId).build();
        // Mock Station Service
        StationDto station = StationDto.builder()
                .id(stationId)
                .controllerPts(controllerPtsDto)
                .modeAffectation(EnumAffectationMode.MANUEL)
                .build();
        lenient().when(stationService.checkedFindById(stationId)).thenReturn(station);
        // Mock Shift Execution Retrieval
        when(shiftPlanningExecutionDao.findByStation(shiftExecutionId, stationId)).thenReturn(Optional.of(shiftExecution));
        when(paymentMethodService.findByStationId(stationId)).thenReturn(List.of(paymentMethodDto));
        PumpTransaction pumpTransaction = new PumpTransaction();
        pumpTransaction.setTotalAmount(BigDecimal.valueOf(25678925));
        pumpTransaction.setTotalVolume(BigDecimal.valueOf(236532));
        lenient().when(transactionService.findFirstTransactionOnDate(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        // Mock Event Publishing
        doNothing().when(eventPublisher).publishEvent(any(EntityActionEvent.class));
         shiftPlanningExecutionService.stop(stationId, shiftExecutionId, stopDateTime, null);

        // Verify method calls
        verify(shiftPlanningExecutionService, times(1)).stop(stationId, shiftExecutionId, stopDateTime, null);
    }
    @Test
    void testStop_ShiftNotInProgress() {
        Long stationId = 1L;
        Long shiftExecutionId = 1L;
        LocalDateTime stopDateTime = LocalDateTime.now();
        // Mocking shift details
        ShiftPlanningExecutionDetailDto shiftDetail = ShiftPlanningExecutionDetailDto.builder()
                .id(1L).pumpId(1L).nozzleId(1L).forced(false)
                .startIndexAmount(BigDecimal.valueOf(25675925)).startIndex(BigDecimal.valueOf(236532)).build();
        // Create mock ShiftPlanningExecutionDto with status NOT IN_PROGRESS
        ShiftPlanningExecutionDto shiftExecution = ShiftPlanningExecutionDto.builder()
                .id(shiftExecutionId)
                .shiftPlanningExecutionDetail(List.of(shiftDetail))
                .status(ShiftExecutionStatus.WAITING)
                .build();

        // Mock Shift Execution Retrieval
        when(shiftPlanningExecutionDao.findByStation(shiftExecutionId, stationId)).thenReturn(Optional.of(shiftExecution));
        lenient().when(transactionService.findFirstTransactionOnDate(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        assertThrows(ServiceValidationException.class,
                () -> shiftPlanningExecutionService.stop(stationId, shiftExecutionId, stopDateTime, null));

    }
    @Test
    void testGenerate_WorkDayShiftPlanningIsNull() {
        Long testStationId = 1L;
        LocalDate testDate = LocalDate.now();

        when(workDayShiftPlanningService.findByStationAndDay(testStationId, testDate)).thenReturn(null);

        // Act
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
            shiftPlanningExecutionService.generate(testDate, testStationId);
        });

        // Assert
        assertEquals("No Scheduled generated !! please generate scheduled for current rotation to access the agenda", exception.getMessage());
    }

    @Test
    void testMapExistingExecutionsWithNullShift() {
        // Arrange
        List<ShiftPlanningExecutionDto> existingExecutions = new ArrayList<>();

        // Mock existing execution details with null shift
        ShiftPlanningExecutionDto executionWithNoShift = ShiftPlanningExecutionDto.builder()
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .shift(null) // Here, the shift is intentionally null
                .index(1)
                .build();

        existingExecutions.add(executionWithNoShift);

        // Mock interaction with mapper
        DynamicShiftPlanningExecutionDto dynamicDto = new DynamicShiftPlanningExecutionDto();
        when(shiftPlanningExecutionDtoMapper.toDynamic(executionWithNoShift)).thenReturn(dynamicDto);

        // Act
        List<DynamicShiftPlanningExecutionDto> result = shiftPlanningExecutionService.mapExistingExecutions(existingExecutions);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(dynamicDto, result.get(0));
    }

    // New test for isOffDay
    @Test
    void testIsOffDay() {
        // Arrange
        shiftDto = ShiftDto.builder()
                .offDay(true)
                .build();

        // Act
        boolean isOffDayResult = shiftPlanningExecutionService.isOffDay(shiftDto);

        // Assert
        assertTrue(isOffDayResult);
    }

    @Test
    void testIsOffDay_NullShiftDto() {
        // Act
        boolean isOffDayResult = shiftPlanningExecutionService.isOffDay(null);

        // Assert
        assertFalse(isOffDayResult);
    }

    // New test for the stop method
    @Test
    void testStop_ShiftExecutionNotFound() {
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 1L;
        LocalDateTime endDateTime = LocalDateTime.now();

        // Mock the behavior of the DAO to return an empty Optional
        when(shiftPlanningExecutionDao.findByStation(shiftPlanningExecutionId, stationId)).thenReturn(Optional.empty());

        // Act & Assert
        ShiftExecutionException exception = assertThrows(ShiftExecutionException.class, () -> {
            shiftPlanningExecutionService.stop(stationId, shiftPlanningExecutionId, endDateTime, request);
        });

        assertEquals("Shift planning execution not found !!.", exception.getMessage());
    }

    @Test
    void testStop_NoPaymentMethods() {
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 1L;
        LocalDateTime endDateTime = LocalDateTime.now();

        // Mock the existing ShiftPlanningExecutionDto
        ShiftPlanningExecutionDto shiftExecution = ShiftPlanningExecutionDto.builder()
                .id(shiftPlanningExecutionId)
                .status(ShiftExecutionStatus.IN_PROGRESS)
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .build();

        // Mock interaction with DAO
        when(shiftPlanningExecutionDao.findByStation(shiftPlanningExecutionId, stationId)).thenReturn(Optional.of(shiftExecution));
        when(paymentMethodService.findByStationId(stationId)).thenReturn(Collections.emptyList()); // No payment methods

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
            shiftPlanningExecutionService.stop(stationId, shiftPlanningExecutionId, endDateTime, request);
        });

        assertEquals("Payment methods not found , Please add one at least to lock this shift !!! ", exception.getMessage());
    }

    @Test
    void testStop_EndTimeBeforeStartTime() {
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 1L;
        LocalDateTime startDateTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endDateTime = LocalDateTime.now().minusHours(2);

        // Mock the existing ShiftPlanningExecutionDto
        ShiftPlanningExecutionDto shiftExecution = ShiftPlanningExecutionDto.builder()
                .id(shiftPlanningExecutionId)
                .startDateTime(startDateTime)
                .endDateTime(LocalDateTime.now().plusHours(1))
                .status(ShiftExecutionStatus.IN_PROGRESS)
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .build();

        // Mock interaction with DAO
        when(shiftPlanningExecutionDao.findByStation(shiftPlanningExecutionId, stationId)).thenReturn(Optional.of(shiftExecution));

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
            shiftPlanningExecutionService.stop(stationId, shiftPlanningExecutionId, endDateTime, request);
        });

        assertEquals("Payment methods not found , Please add one at least to lock this shift !!! ", exception.getMessage());
    }

    @Test
    void testIsOffDay_WhenOffDayIsFalse() {
        // Arrange
        shiftDto = ShiftDto.builder().offDay(false).build();

        // Act
        boolean isOffDayResult = shiftPlanningExecutionService.isOffDay(shiftDto);

        // Assert
        assertFalse(isOffDayResult);
    }

    @Test
    void testGenerateExecutionDetails_PerPumpAttendant_WithNullValues() {
        // Arrange
        AffectedPumpAttendantDto nullPumpAttendantDto = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            shiftPlanningExecutionService.generateExecutionDetailsPerPumpAttendant(nullPumpAttendantDto, workDayShiftPlanningDto, shiftPlanningDto);
        });
    }

    @Test
    void testGenerate_WithExistingShiftPlanningExecutions() {
        // Arrange
        LocalDate today = LocalDate.now();
        Long testStationId = 1L;
        PumpAttendantCollectionSheetDto collectionSheetDto = PumpAttendantCollectionSheetDto.builder().build();
        WorkDayShiftPlanningDto workDayShiftPlanning = WorkDayShiftPlanningDto.builder().id(testStationId).day(today).build();
        ShiftPlanningExecutionDto executionDto = ShiftPlanningExecutionDto.builder()
                .id(1L)
                .shift(shiftDto)
                .pumpAttendantCollectionSheets(Collections.singletonList(collectionSheetDto))
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .build();

        when(workDayShiftPlanningService.findByStationAndDay(testStationId, today)).thenReturn(workDayShiftPlanning);
        when(shiftPlanningExecutionDao.findByWorkDayAndStation(testStationId, workDayShiftPlanning.getId())).thenReturn(List.of(executionDto));

        // Act
        List<DynamicShiftPlanningExecutionDto> result = shiftPlanningExecutionService.generate(today, testStationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testLock_ShiftPlanningExecutionAlreadyLocked() {
        // Arrange
        Long stationId = 1L;
        Long shiftPlanningExecutionId = 1L;
        PumpAttendantCollectionSheetDto collectionSheetDto = PumpAttendantCollectionSheetDto.builder().build();
        shiftPlanningExecutionDto = ShiftPlanningExecutionDto.builder()
                .id(shiftPlanningExecutionId)
                .status(ShiftExecutionStatus.LOCKED) // Set to LOCKED
                .pumpAttendantCollectionSheets(Collections.singletonList(collectionSheetDto))
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .build();

        when(shiftPlanningExecutionDao.findById(shiftPlanningExecutionId)).thenReturn(Optional.of(shiftPlanningExecutionDto));

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () -> {
            shiftPlanningExecutionService.lock(stationId, shiftPlanningExecutionId, request);
        });

        assertEquals("Shift planning execution is at status :[LOCKED] and can't be LOCKED !!! ", exception.getMessage());
    }

    @Test
    void testUpdateShiftDetails_NullUpdateRequest() {
        // Arrange
        ShiftDetailUpdatesDto shiftDetailUpdatesDto = ShiftDetailUpdatesDto.builder()
                .shiftPlanningExecutionId(1L)
                .id(1L)
                .pumpAttendantId(1L)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .build();

        // Act & Assert
        assertThrows(ShiftExecutionException.class, () -> {
            shiftPlanningExecutionService.updateShiftDetails(shiftDetailUpdatesDto, null);
        });
    }

    private AffectedPumpAttendantDto createAffectedPumpAttendantDto() {
        return AffectedPumpAttendantDto.builder()
                .pumpAttendantId(1L)
                .pumpId(1L)
                .build();
    }
    private WorkDayShiftPlanningDto createWorkDayShiftPlanningDto() {
        return WorkDayShiftPlanningDto.builder()
                .id(1L)
                .day(LocalDate.of(2023, 10, 10))
                .build();
    }
    private ControllerPtsDto createControllerPtsDto() {
        return ControllerPtsDto.builder()
                .id(1L)
                .ptsId("0027003A3438510935383135")
                .build();
    }
    private StationDto createStationDto() {
        return StationDto.builder()
                .id(1L)
                .controllerPts(controllerPtsDto)
                .build();
    }
    private ShiftDto createShiftDto() {
        return ShiftDto.builder()
                .id(1L)
                .startingTime(LocalTime.of(8, 0))
                .endingTime(LocalTime.of(16, 0))
                .build();
    }
    private ShiftPlanningDto createShiftPlanningDto() {
        return ShiftPlanningDto.builder()
                .id(1L)
                .shift(shiftDto)
                .workDayShiftPlanningId(1L)
                .pumpAttendantTeamId(1L)
                .build();
    }
    private ShiftPlanningExecutionDto createShiftPlanningExecutionDto(){
        return ShiftPlanningExecutionDto.builder()
                .id(1L)
                .shift(shiftDto)
                .status(ShiftExecutionStatus.COMPLETED)
                .shiftPlanningExecutionDetail(new ArrayList<>())
                .pumpAttendantCollectionSheets(new ArrayList<>())
                .build();
    }
}
