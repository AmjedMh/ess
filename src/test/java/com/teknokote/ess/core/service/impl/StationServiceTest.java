package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.StationDao;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.model.requests.EnumRequestStatus;
import com.teknokote.ess.core.repository.FuelGradesRepository;
import com.teknokote.ess.core.service.impl.validators.StationValidator;
import com.teknokote.ess.core.service.mappers.FuelGradeConfigMapper;
import com.teknokote.ess.core.service.requests.FuelGradePriceChangeRequestService;
import com.teknokote.ess.dto.*;
import com.teknokote.ess.dto.requests.FuelGradePriceChangeRequestDto;
import com.teknokote.ess.http.logger.EntityActionEvent;
import com.teknokote.ess.wsserveur.ESSWebSocketHandler;
import com.teknokote.pts.client.request.set.SetUsersConfigurationRequestPacket;
import com.teknokote.pts.client.response.configuration.PTSDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {
    @InjectMocks
    private StationService stationService;
    @Mock
    private FuelGradePriceChangeRequestService fuelGradePriceChangeRequestService;
    @Mock
    private ESSWebSocketHandler essWebSocketHandler;
    @Mock
    private FuelGradeConfigMapper fuelGradeConfigMapper;
    @Mock
    private FuelGradesRepository fuelGradesRepository;
    @Mock
    private StationDao stationDao;
    @Mock
    private StationValidator stationValidator;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ControllerPtsConfigurationService controllerPtsConfigurationService;
    private User mockUser;
    private StationDto mockStationDto;
    private FuelGradeConfigDto mockFuelGradeConfigDto;
    @Mock
    private FuelGradePriceChangeRequestDto mockRequestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        mockUser = new User();
        mockUser.setId(1L);

        mockStationDto = StationDto.builder().id(1L).build();
        mockStationDto.setId(1L);
        mockStationDto.setName("Test Station");
        mockStationDto.setCustomerAccountName("Test Customer");

        mockFuelGradeConfigDto = new FuelGradeConfigDto();
        mockFuelGradeConfigDto.setId(1L);
        mockFuelGradeConfigDto.setName("Gasoil");
        mockFuelGradeConfigDto.setPlannedDate(LocalDateTime.now());
        mockFuelGradeConfigDto.setScheduledDate(LocalDateTime.now());
        mockFuelGradeConfigDto.setPrice(2.5);
        FuelGrade fuelGrade = new FuelGrade();
        fuelGrade.setName("Diesel");
        mockRequestDto = FuelGradePriceChangeRequestDto.builder().plannedDate(LocalDateTime.now().plusDays(1)).build();
        mockRequestDto.setId(100L);
        mockRequestDto.setOldPrice(2150.00);
        mockRequestDto.setNewPrice(2200.00);
        mockRequestDto.setFuelGrade(fuelGrade);
        mockRequestDto.setStatus(EnumRequestStatus.PLANNED);
    }

    @Test
    void testPlanFuelGradePriceChange() {
        when(stationService.findById(1L)).thenReturn(Optional.of(mockStationDto));
        stationService.planFuelGradePriceChange(1L, mockUser, mockFuelGradeConfigDto, null);
        verify(fuelGradePriceChangeRequestService, times(1)).create(any(FuelGradePriceChangeRequestDto.class));
        verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
    }

    @Test
    void testCancelFuelGradePriceChangePlanification_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(stationService.findById(1L)).thenReturn(Optional.of(mockStationDto));
        when(fuelGradePriceChangeRequestService.checkedFindById(1L)).thenReturn(mockRequestDto);

        stationService.cancelFuelGradePriceChangePlanification(1L, 1L, null);

        Assertions.assertEquals(EnumRequestStatus.CANCELED, mockRequestDto.getStatus());
        verify(fuelGradePriceChangeRequestService, times(1)).update(mockRequestDto);
        verify(eventPublisher, times(1)).publishEvent(any(EntityActionEvent.class));
    }

    @Test
    void testChangeFuelGradePrices_Success() {
        ControllerPtsDto controllerPtsDto = ControllerPtsDto.builder().id(1L).ptsId("PTS-1").build();
        mockStationDto.setControllerPts(controllerPtsDto);
        when(stationDao.findById(1L)).thenReturn(Optional.of(mockStationDto));
        when(controllerPtsConfigurationService.findCurrentConfigurationOnController(1L))
                .thenReturn(new ControllerPtsConfiguration());
        when(fuelGradesRepository.findFuelGradesByControllerConfiguration(any()))
                .thenReturn(List.of());
        when(fuelGradeConfigMapper.toPtsFuelGradeList(any())).thenReturn(List.of());
        stationService.changeFuelGradePrices(1L, mockFuelGradeConfigDto);

        verify(essWebSocketHandler, times(1)).sendRequests(eq("PTS-1"), any());
    }

    @Test
    void testGetControllerDateTime() {
        ControllerPtsDto controllerPtsDto = ControllerPtsDto.builder().ptsId("PTS-1").build();
        mockStationDto.setControllerPts(controllerPtsDto);
        when(stationService.findById(1L)).thenReturn(Optional.of(mockStationDto));
        when(essWebSocketHandler.getDateTimeFromController(eq("PTS-1"), any())).thenReturn(new PTSDateTime());
        PTSDateTime result = stationService.getControllerDateTime(1L);
        assertNotNull(result);
        verify(essWebSocketHandler, times(1)).getDateTimeFromController(eq("PTS-1"), any());
    }

    @Test
    void testGetControllerUploadedInformation() {
        when(essWebSocketHandler.getUploadedInformationFromController(eq("1"), any())).thenReturn(UploadedRecordsInformationDto.builder().build());

        UploadedRecordsInformationDto result = stationService.getControllerUploadedInformation("1");

        assertNotNull(result);
        verify(essWebSocketHandler, times(1)).getUploadedInformationFromController(eq("1"), any());
    }

    @Test
    void testChangeUserController_shouldSendSetUsersConfigurationRequestPacket() {
        Long stationId = 1L;
        String testUsername = "testUser";
        String testPassword = "secret";
        ControllerPtsDto controllerPtsDto = ControllerPtsDto.builder().ptsId("PTS-1").build();
        mockStationDto.setControllerPts(controllerPtsDto);
        UserDto userDto = UserDto.builder().build();
        userDto.setUsername(testUsername);
        userDto.setPassword(testPassword);

        when(stationService.findById(stationId)).thenReturn(Optional.of(mockStationDto));

        stationService.changeUserController(stationId, userDto);

        // Assert
        verify(essWebSocketHandler, times(1)).sendRequests(eq("PTS-1"), argThat(requests -> {
            if (requests.size() != 1) return false;
            var request = requests.get(0);
            if (!(request instanceof SetUsersConfigurationRequestPacket packet)) return false;
            var users = packet.getData().getUsers();
            return users.size() == 1 &&
                    users.get(0).getLogin().equals(testUsername) &&
                    users.get(0).getPassword().equals(testPassword) &&
                    users.get(0).getPermission().getControl() &&
                    users.get(0).getPermission().getMonitoring() &&
                    users.get(0).getPermission().getConfiguration() &&
                    users.get(0).getPermission().getReports();
        }));
    }

    @Test
    void testGetFuelGradeByStationAndConfiguration() {
        // Setup
        Long stationId = 1L;
        Long idConf = 2L;

        // Mock inputs
        ControllerPtsConfiguration mockCurrentConfiguration = new ControllerPtsConfiguration();
        FuelGrade mockFuelGrade = new FuelGrade();
        mockFuelGrade.setId(idConf);
        mockFuelGrade.setName("Sample Fuel Grade");

        // Create a mock StationDto which has a ControllerPts
        mockStationDto.setControllerPts(ControllerPtsDto.builder().id(1L).ptsId("PTS-1").build());

        // Set expectations on the mock objects
        when(stationService.findById(stationId)).thenReturn(Optional.of(mockStationDto));
        when(controllerPtsConfigurationService.findCurrentConfigurationOnController(mockStationDto.getControllerPts().getId()))
                .thenReturn(mockCurrentConfiguration);
        when(fuelGradesRepository.findFuelGradesByControllerConfigurationAndIdConf(mockCurrentConfiguration, idConf))
                .thenReturn(mockFuelGrade);
        when(fuelGradeConfigMapper.toDtoFuelGrade(mockFuelGrade)).thenReturn(mockFuelGradeConfigDto);

        // Execute the method under test
        FuelGradeConfigDto result = stationService.getFuelGradeByStationAndConfiguration(stationId, idConf);

        // Assert
        assertNotNull(result);
        assertEquals(mockFuelGradeConfigDto, result);
        verify(controllerPtsConfigurationService).findCurrentConfigurationOnController(mockStationDto.getControllerPts().getId());
        verify(fuelGradesRepository).findFuelGradesByControllerConfigurationAndIdConf(mockCurrentConfiguration, idConf);
        verify(fuelGradeConfigMapper).toDtoFuelGrade(mockFuelGrade);
    }
    @Test
    void testGetFuelGradeByStation() {
        // Setup
        Long stationId = 1L;
        Long fuelId = 2L;

        // Mocking the required objects
        ControllerPtsConfiguration mockCurrentConfiguration = new ControllerPtsConfiguration();
        FuelGrade mockFuelGrade = new FuelGrade();
        mockFuelGrade.setId(fuelId);
        mockFuelGrade.setName("Petrol");

        // Create a mock StationDto which contains a ControllerPts
        mockStationDto.setControllerPts(ControllerPtsDto.builder().id(1L).ptsId("PTS-1").build());

        // Set expectations on the mock objects
        when(stationService.findById(stationId)).thenReturn(Optional.of(mockStationDto));
        when(controllerPtsConfigurationService.findCurrentConfigurationOnController(mockStationDto.getControllerPts().getId()))
                .thenReturn(mockCurrentConfiguration);
        when(fuelGradesRepository.findFuelGradesByControllerConfigurationAndId(mockCurrentConfiguration, fuelId))
                .thenReturn(mockFuelGrade);
        when(fuelGradeConfigMapper.toDtoFuelGrade(mockFuelGrade)).thenReturn(mockFuelGradeConfigDto);

        // Execute the method under test
        FuelGradeConfigDto result = stationService.getFuelGradeByStation(stationId, fuelId);

        // Assertions
        assertNotNull(result);
        assertEquals(mockFuelGradeConfigDto, result);
        verify(controllerPtsConfigurationService).findCurrentConfigurationOnController(mockStationDto.getControllerPts().getId());
        verify(fuelGradesRepository).findFuelGradesByControllerConfigurationAndId(mockCurrentConfiguration, fuelId);
        verify(fuelGradeConfigMapper).toDtoFuelGrade(mockFuelGrade);
    }
    @Test
    void testFindExecutedFuelGradePriceChangeByStation() {
        // Given
        Long stationId = 1L;
        String fuelName = "Petrol";
        String requesterName = "testUser";
        String price = "2200.00";
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);

        FuelGrade fuelGrade = new FuelGrade();
        fuelGrade.setName("Petrol");

        UserDto requester = UserDto.builder().username(requesterName).build();

        // Create mock data
        FuelGradePriceChangeRequestDto mockRequestDto1 = FuelGradePriceChangeRequestDto.builder()
                .fuelGrade(fuelGrade)
                .requester(requester) // requester is not null
                .newPrice(2200.00)
                .plannedDate(LocalDateTime.now().minusDays(1))
                .build();

        FuelGradePriceChangeRequestDto mockRequestDto2 = FuelGradePriceChangeRequestDto.builder()
                .fuelGrade(fuelGrade)
                .requester(null) // This should be null and handled in the method to prevent NPE
                .newPrice(2300.00)
                .plannedDate(LocalDateTime.now().minusDays(3))
                .build();

        FuelGradePriceChangeRequestDto mockRequestDto3 = FuelGradePriceChangeRequestDto.builder()
                .fuelGrade(fuelGrade)
                .requester(requester) // requester is not null
                .newPrice(2000.00)
                .plannedDate(LocalDateTime.now().plusDays(1))
                .build();

        // only mock executed changes for past dates that should return results
        List<FuelGradePriceChangeRequestDto> mockExecutedChanges = List.of(mockRequestDto1, mockRequestDto2, mockRequestDto3);

        // Mocking the executed changes retrieval
        when(fuelGradePriceChangeRequestService.findExecutedByStation(stationId)).thenReturn(mockExecutedChanges);

        // When
        List<FuelGradePriceChangeRequestDto> result = stationService.findExecutedFuelGradePriceChangeByStation(
                stationId, fuelName, null, price, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockRequestDto1, result.get(0));

        // Verify interactions
        verify(fuelGradePriceChangeRequestService).findExecutedByStation(stationId);
    }
    @Test
    void testFindPlannedFuelGradePriceChangeByStation() {
        // Given
        Long stationId = 1L;
        String fuelName = "Petrol";
        String requesterName = "testUser";
        String price = "2200.00";
        LocalDateTime startDate = LocalDateTime.now().minusDays(5);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        FuelGrade fuelGrade = new FuelGrade();
        fuelGrade.setName("Petrol");

        UserDto requester = UserDto.builder().username(requesterName).build();

        // Creating mock data
        FuelGradePriceChangeRequestDto plannedRequestDto1 = FuelGradePriceChangeRequestDto.builder()
                .fuelGrade(fuelGrade)
                .requester(requester) // non-null requester
                .newPrice(2200.00) // Matching price
                .plannedDate(LocalDateTime.now().plusDays(1)) // Within start and end date
                .status(EnumRequestStatus.PLANNED) // Not executed
                .build();

        FuelGradePriceChangeRequestDto plannedRequestDto2 = FuelGradePriceChangeRequestDto.builder()
                .fuelGrade(fuelGrade)
                .requester(requester) // non-null requester
                .newPrice(2100.00) // Not matching price
                .plannedDate(LocalDateTime.now().plusDays(2)) // Within start and end date
                .status(EnumRequestStatus.EXECUTED) // Executed, should be filtered out
                .build();

        FuelGradePriceChangeRequestDto plannedRequestDto3 = FuelGradePriceChangeRequestDto.builder()
                .fuelGrade(fuelGrade)
                .requester(null) // null requester
                .newPrice(2200.00) // Matching price
                .plannedDate(LocalDateTime.now().plusDays(3)) // Within start and end date
                .status(EnumRequestStatus.PLANNED) // Not executed
                .build();

        FuelGradePriceChangeRequestDto plannedRequestDto4 = FuelGradePriceChangeRequestDto.builder()
                .fuelGrade(fuelGrade)
                .requester(requester) // non-null requester
                .newPrice(2200.00) // Matching price
                .plannedDate(LocalDateTime.now().minusDays(6)) // Outside start date
                .status(EnumRequestStatus.PLANNED) // Not executed
                .build();

        List<FuelGradePriceChangeRequestDto> mockPlannedChanges = List.of(plannedRequestDto1, plannedRequestDto2, plannedRequestDto3, plannedRequestDto4);

        // Mocking the service to return planned changes
        when(fuelGradePriceChangeRequestService.findPlannedByStation(stationId)).thenReturn(mockPlannedChanges);
        when(stationService.findById(stationId)).thenReturn(Optional.of(mockStationDto));
        // When
        List<FuelGradePriceChangeRequestDto> result = stationService.findPlannedFuelGradePriceChangeByStation(
                stationId, fuelName, null, price, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify interactions
        verify(fuelGradePriceChangeRequestService).findPlannedByStation(stationId);
    }
    @Test
    void testFindPlannedFuelGradePriceChangeByStation_FuelNameFiltering() {
        // Setup
        Long stationId = 1L;
        String fuelName = "Petrol"; // this should match the fuel name in plannedRequestDto1
        String requesterName = null; // we are ignoring requester
        String price = null; // we are ignoring price
        LocalDateTime startDate = null; // we are ignoring start date
        LocalDateTime endDate = null; // we are ignoring end date

        // Create mock planned changes
        List<FuelGradePriceChangeRequestDto> mockPlannedChanges = List.of(
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Petrol"); } }) // should match
                        .status(EnumRequestStatus.PLANNED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2200.00)
                        .build(),
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Diesel"); } })
                        .status(EnumRequestStatus.PLANNED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2100.00)
                        .build()
        );

        // Mocking the service to return the planned changes
        when(fuelGradePriceChangeRequestService.findPlannedByStation(stationId)).thenReturn(mockPlannedChanges);
        when(stationService.findById(1L)).thenReturn(Optional.ofNullable(mockStationDto));
        // Execute
        List<FuelGradePriceChangeRequestDto> result = stationService.findPlannedFuelGradePriceChangeByStation(
                stationId, fuelName, requesterName, price, startDate, endDate);

        // Assertions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Petrol", result.get(0).getFuelGrade().getName());
    }

    @Test
    void testFindPlannedFuelGradePriceChangeByStation_RequesterNameFiltering() {
        // Setup
        Long stationId = 1L;
        String fuelName = null;
        String requesterName = "testUser"; // should match mockRequestDto1
        String price = null;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        // Create mock planned changes
        List<FuelGradePriceChangeRequestDto> mockPlannedChanges = List.of(
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Petrol"); } })
                        .requester(UserDto.builder().username("testUser").build()) // should match
                        .status(EnumRequestStatus.PLANNED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2200.00)
                        .build(),
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Diesel"); } })
                        .requester(UserDto.builder().username("anotherUser").build())
                        .status(EnumRequestStatus.PLANNED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2100.00)
                        .build()
        );

        // Mocking
        when(fuelGradePriceChangeRequestService.findPlannedByStation(stationId)).thenReturn(mockPlannedChanges);
        when(stationService.findById(1L)).thenReturn(Optional.ofNullable(mockStationDto));

        // Execute
        List<FuelGradePriceChangeRequestDto> result = stationService.findPlannedFuelGradePriceChangeByStation(
                stationId, fuelName, requesterName, price, startDate, endDate);

        // Assertions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testUser", result.get(0).getRequester().getUsername());
    }

    @Test
    void testFindPlannedFuelGradePriceChangeByStation_PriceFiltering() {
        // Setup
        Long stationId = 1L;
        String fuelName = null;
        String requesterName = null;
        String price = "2200.00"; // Should match plannedRequestDto1
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        // Create mock planned changes
        List<FuelGradePriceChangeRequestDto> mockPlannedChanges = List.of(
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Petrol"); } })
                        .requester(UserDto.builder().username("testUser").build())
                        .status(EnumRequestStatus.PLANNED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2200.00) // should match
                        .build(),
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Diesel"); } })
                        .requester(UserDto.builder().username("anotherUser").build())
                        .status(EnumRequestStatus.PLANNED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2100.00)
                        .build()
        );

        // Mocking the service to return planned changes
        when(fuelGradePriceChangeRequestService.findPlannedByStation(stationId)).thenReturn(mockPlannedChanges);
        when(stationService.findById(1L)).thenReturn(Optional.ofNullable(mockStationDto));

        // Execute
        List<FuelGradePriceChangeRequestDto> result = stationService.findPlannedFuelGradePriceChangeByStation(
                stationId, fuelName, requesterName, price, startDate, endDate);

        // Assertions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2200.00, result.get(0).getNewPrice());
    }

    @Test
    void testFindPlannedFuelGradePriceChangeByStation_DateFiltering() {
        // Setup
        Long stationId = 1L;
        String fuelName = null;
        String requesterName = null;
        String price = null;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1); // Only planned after this date
        LocalDateTime endDate = LocalDateTime.now().plusDays(1); // Up to this date

        // Create mock planned changes
        List<FuelGradePriceChangeRequestDto> mockPlannedChanges = List.of(
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Petrol"); } })
                        .status(EnumRequestStatus.PLANNED)
                        .plannedDate(LocalDateTime.now()) // should match
                        .newPrice(2200.00)
                        .build(),
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Diesel"); } })
                        .status(EnumRequestStatus.PLANNED)
                        .plannedDate(LocalDateTime.now().minusDays(2))
                        .newPrice(2100.00)
                        .build()
        );

        // Mocking the service to return planned changes
        when(fuelGradePriceChangeRequestService.findPlannedByStation(stationId)).thenReturn(mockPlannedChanges);
        when(stationService.findById(1L)).thenReturn(Optional.ofNullable(mockStationDto));

        // Execute
        List<FuelGradePriceChangeRequestDto> result = stationService.findPlannedFuelGradePriceChangeByStation(
                stationId, fuelName, requesterName, price, startDate, endDate);

        // Assertions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Petrol", result.get(0).getFuelGrade().getName());
        assertEquals(2200.00, result.get(0).getNewPrice());
    }

    @Test
    void testFindExecutedFuelGradePriceChangeByStation_FuelNameFiltering() {
        // Setup
        Long stationId = 1L;
        String fuelName = "Petrol"; // This should match the fuel name in plannedRequestDto1
        String requesterName = null; // We are ignoring requester
        String price = null; // We are ignoring price
        LocalDateTime startDate = null; // We are ignoring start date
        LocalDateTime endDate = null; // We are ignoring end date

        // Create mock executed changes
        List<FuelGradePriceChangeRequestDto> mockExecutedChanges = List.of(
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Petrol"); }}) // should match
                        .status(EnumRequestStatus.EXECUTED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2200.00)
                        .build(),
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Diesel"); }})
                        .status(EnumRequestStatus.EXECUTED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2100.00)
                        .build()
        );

        // Mocking the service to return the executed changes
        when(fuelGradePriceChangeRequestService.findExecutedByStation(stationId)).thenReturn(mockExecutedChanges);

        // Execute
        List<FuelGradePriceChangeRequestDto> result = stationService.findExecutedFuelGradePriceChangeByStation(
                stationId, fuelName, requesterName, price, startDate, endDate);

        // Assertions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Petrol", result.get(0).getFuelGrade().getName());
    }

    @Test
    void testFindExecutedFuelGradePriceChangeByStation_RequesterNameFiltering() {
        // Setup
        Long stationId = 1L;
        String fuelName = null;
        String requesterName = "testUser"; // should match mockRequestDto1
        String price = null;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        // Create mock executed changes
        List<FuelGradePriceChangeRequestDto> mockExecutedChanges = List.of(
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Petrol"); }})
                        .requester(UserDto.builder().username("testUser").build()) // should match
                        .status(EnumRequestStatus.EXECUTED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2200.00)
                        .build(),
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Diesel"); }})
                        .requester(UserDto.builder().username("anotherUser").build())
                        .status(EnumRequestStatus.EXECUTED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2100.00)
                        .build()
        );

        // Mocking
        when(fuelGradePriceChangeRequestService.findExecutedByStation(stationId)).thenReturn(mockExecutedChanges);

        // Execute
        List<FuelGradePriceChangeRequestDto> result = stationService.findExecutedFuelGradePriceChangeByStation(
                stationId, fuelName, requesterName, price, startDate, endDate);

        // Assertions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testUser", result.get(0).getRequester().getUsername());
    }

    @Test
    void testFindExecutedFuelGradePriceChangeByStation_PriceFiltering() {
        // Setup
        Long stationId = 1L;
        String fuelName = null;
        String requesterName = null;
        String price = "2200.00"; // Should match plannedRequestDto1
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        // Create mock executed changes
        List<FuelGradePriceChangeRequestDto> mockExecutedChanges = List.of(
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Petrol"); }})
                        .requester(UserDto.builder().username("testUser").build())
                        .status(EnumRequestStatus.EXECUTED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2200.00) // should match
                        .build(),
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Diesel"); }})
                        .requester(UserDto.builder().username("anotherUser").build())
                        .status(EnumRequestStatus.EXECUTED)
                        .plannedDate(LocalDateTime.now())
                        .newPrice(2100.00)
                        .build()
        );

        // Mocking the service to return executed changes
        when(fuelGradePriceChangeRequestService.findExecutedByStation(stationId)).thenReturn(mockExecutedChanges);

        // Execute
        List<FuelGradePriceChangeRequestDto> result = stationService.findExecutedFuelGradePriceChangeByStation(
                stationId, fuelName, requesterName, price, startDate, endDate);

        // Assertions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2200.00, result.get(0).getNewPrice());
    }

    @Test
    void testFindExecutedFuelGradePriceChangeByStation_DateFiltering() {
        // Setup
        Long stationId = 1L;
        String fuelName = null;
        String requesterName = null;
        String price = null;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1); // Only executed after this date
        LocalDateTime endDate = LocalDateTime.now().plusDays(1); // Up to this date

        // Create mock executed changes
        List<FuelGradePriceChangeRequestDto> mockExecutedChanges = List.of(
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Petrol"); }})
                        .status(EnumRequestStatus.EXECUTED)
                        .plannedDate(LocalDateTime.now()) // should match
                        .newPrice(2200.00)
                        .build(),
                FuelGradePriceChangeRequestDto.builder()
                        .fuelGrade(new FuelGrade() {{ setName("Diesel"); }})
                        .status(EnumRequestStatus.EXECUTED)
                        .plannedDate(LocalDateTime.now().minusDays(2))
                        .newPrice(2100.00)
                        .build()
        );

        // Mocking the service to return executed changes
        when(fuelGradePriceChangeRequestService.findExecutedByStation(stationId)).thenReturn(mockExecutedChanges);

        // Execute
        List<FuelGradePriceChangeRequestDto> result = stationService.findExecutedFuelGradePriceChangeByStation(
                stationId, fuelName, requesterName, price, startDate, endDate);

        // Assertions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Petrol", result.get(0).getFuelGrade().getName());
        assertEquals(2200.00, result.get(0).getNewPrice());
    }
}
