package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.ess.core.dao.PumpAttendantDao;
import com.teknokote.ess.core.dao.organization.PumpAttendantTeamDao;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.impl.shifts.PumpAttendantTeamServiceImpl;
import com.teknokote.ess.core.service.impl.validators.organization.PumpAttendantTeamValidator;
import com.teknokote.ess.core.service.shifts.ShiftRotationService;
import com.teknokote.ess.dto.PumpAttendantDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.organization.DynamicPumpAttendantTeamDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import com.teknokote.ess.dto.shifts.AffectedPumpAttendantDto;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PumpAttendantTeamServiceImplTest {

    @InjectMocks
    private PumpAttendantTeamServiceImpl pumpAttendantTeamService;
    @Mock
    private ESSValidationResult validationResult;
    @Mock
    private PumpAttendantTeamValidator pumpAttendantTeamValidator;
    @Mock
    private PumpAttendantTeamDao dao;
    @Mock
    private StationService stationService;
    @Mock
    private ShiftRotationService shiftRotationService;
    @Mock
    private PumpAttendantDao pumpAttendantDao;
    @Mock
    private PumpAttendantTeamDao pumpAttendantTeamDao;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void testAddTeam_Success() {
        // Arrange
        Long stationId = 1L;
        Long shiftRotationId = 100L;
        Long pumpAttendantId = 10L;
        User mockUser = new User();
        mockUser.setUsername("admin");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        PumpAttendantTeamDto teamDto = PumpAttendantTeamDto.builder().build();
        when(pumpAttendantTeamValidator.validateOnCreate(teamDto)).thenReturn(validationResult);
        teamDto.setStationId(stationId);
        teamDto.setShiftRotationId(shiftRotationId);
        teamDto.setName("Morning Shift");

        AffectedPumpAttendantDto affectedPumpAttendant = AffectedPumpAttendantDto.builder().build();
        affectedPumpAttendant.setPumpAttendantId(pumpAttendantId);
        affectedPumpAttendant.setPumpAttendant(PumpAttendantDto.builder().build());
        teamDto.setAffectedPumpAttendant(Set.of(affectedPumpAttendant));

        StationDto stationDto = StationDto.builder().build();
        stationDto.setId(stationId);
        stationDto.setName("Station A");
        stationDto.setCustomerAccountName("Customer X");

        when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
        when(pumpAttendantTeamService.findTeamsByRotationAndPumpAttendant(shiftRotationId, pumpAttendantId))
                .thenReturn(Collections.emptyList());
        when(pumpAttendantTeamService.getDao().create(teamDto)).thenReturn(teamDto);

        // Act
        PumpAttendantTeamDto result = pumpAttendantTeamService.addTeam(stationId, teamDto, request);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
    }
    @Test
    void testUpdateTeam_Success() {
        // Setup
        Long stationId = 1L;
        User mockUser = new User();

        AffectedPumpAttendantDto affectedDto = AffectedPumpAttendantDto.builder()
                .pumpAttendantTeamId(1L)
                .build();

        Set<AffectedPumpAttendantDto> affectedPumpAttendants = new HashSet<>();
        affectedPumpAttendants.add(affectedDto);

        PumpAttendantTeamDto teamDto = PumpAttendantTeamDto.builder()
                .id(1L)
                .stationId(stationId)
                .name("Team A")
                .affectedPumpAttendant(affectedPumpAttendants)
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        when(pumpAttendantTeamValidator.validateOnUpdate(teamDto)).thenReturn(validationResult);
        when(stationService.checkedFindById(stationId)).thenReturn(StationDto.builder().id(1L).build());
        when(dao.findById(1L)).thenReturn(Optional.of(teamDto));
        when(dao.update(any())).thenReturn(teamDto);

        // Execute
        PumpAttendantTeamDto result = pumpAttendantTeamService.updateTeam(stationId, teamDto, request);

        // Verify
        assertNotNull(result);
        assertEquals("Team A", result.getName());
        Mockito.verify(dao).update(teamDto);
    }
    @Test
    void testGetDynamicPumpAttendantTeams() {
        // Prepare test data
        Long stationId = 1L;
        Long shiftRotationId = 1L;

        PumpAttendantDto pumpAttendantDto = PumpAttendantDto.builder()
                .id(1L)
                .build();

        AffectedPumpAttendantDto affectedDto = AffectedPumpAttendantDto.builder()
                .id(1L)
                .pumpId(1L)
                .pumpAttendant(pumpAttendantDto)
                .pumpAttendantTeamId(1L)
                .build();

        Set<AffectedPumpAttendantDto> affectedPumpAttendants = new HashSet<>();
        affectedPumpAttendants.add(affectedDto);

        PumpAttendantTeamDto teamDto = PumpAttendantTeamDto.builder()
                .id(1L)
                .stationId(stationId)
                .name("Team A")
                .shiftRotationId(shiftRotationId)
                .affectedPumpAttendant(affectedPumpAttendants)
                .build();

        when(pumpAttendantTeamService.findByStationAndRotation(stationId, shiftRotationId))
                .thenReturn(Arrays.asList(teamDto));

        // Execute the service method
        List<DynamicPumpAttendantTeamDto> dynamicTeams = pumpAttendantTeamService.getDynamicPumpAttendantTeams(stationId, shiftRotationId);

        // Verify the result
        assertNotNull(dynamicTeams);
        assertEquals(1, dynamicTeams.size());

        DynamicPumpAttendantTeamDto dynamicTeam = dynamicTeams.get(0);

        assertNotNull(dynamicTeam.getPumpAttendantToPump());
        assertFalse(dynamicTeam.getPumpAttendantToPump().isEmpty());

        Map<Long, PumpAttendantDto> pumpAttendantToPump = dynamicTeam.getPumpAttendantToPump();
        assertEquals(1, pumpAttendantToPump.size());

        PumpAttendantDto retrievedAttendant = pumpAttendantToPump.get(1L);
        assertNotNull(retrievedAttendant);
        assertEquals(1L, retrievedAttendant.getId());
    }
    @Test
    void testGetDynamicPumpAttendantTeam() {
        // Prepare test data
        Long stationId = 1L;

        PumpAttendantDto pumpAttendantDto = PumpAttendantDto.builder()
                .id(1L)
                .build();

        AffectedPumpAttendantDto affectedDto = AffectedPumpAttendantDto.builder()
                .id(1L)
                .pumpId(1L)
                .pumpAttendant(pumpAttendantDto)
                .pumpAttendantTeamId(1L)
                .build();

        Set<AffectedPumpAttendantDto> affectedPumpAttendants = new HashSet<>();
        affectedPumpAttendants.add(affectedDto);

        PumpAttendantTeamDto teamDto = PumpAttendantTeamDto.builder()
                .id(1L)
                .stationId(stationId)
                .name("Team A")
                .shiftRotationId(1L)
                .affectedPumpAttendant(affectedPumpAttendants)
                .build();

        when(pumpAttendantTeamService.findByStation(stationId))
                .thenReturn(Arrays.asList(teamDto));

        // Execute the service method
        List<DynamicPumpAttendantTeamDto> dynamicTeams = pumpAttendantTeamService.getDynamicPumpAttendantTeam(stationId);

        // Verify the result
        assertNotNull(dynamicTeams);
        assertEquals(1, dynamicTeams.size());

        DynamicPumpAttendantTeamDto dynamicTeam = dynamicTeams.get(0);

        assertNotNull(dynamicTeam.getPumpAttendantToPump());
        assertFalse(dynamicTeam.getPumpAttendantToPump().isEmpty());

        Map<Long, PumpAttendantDto> pumpAttendantToPump = dynamicTeam.getPumpAttendantToPump();
        assertEquals(1, pumpAttendantToPump.size());

        PumpAttendantDto retrievedAttendant = pumpAttendantToPump.get(1L);
        assertNotNull(retrievedAttendant);
        assertEquals(1L, retrievedAttendant.getId());
    }
    @Test
    void testFindByPumpAttendant() {
        // Arrange
        Long pumpAttendantId = 10L;
        PumpAttendantTeamDto teamDto1 = PumpAttendantTeamDto.builder().id(1L).name("Team A").build();
        PumpAttendantTeamDto teamDto2 = PumpAttendantTeamDto.builder().id(2L).name("Team B").build();
        List<PumpAttendantTeamDto> expectedTeams = Arrays.asList(teamDto1, teamDto2);

        when(dao.findByPumpAttendant(pumpAttendantId)).thenReturn(expectedTeams);

        // Act
        List<PumpAttendantTeamDto> result = pumpAttendantTeamService.findByPumpAttendant(pumpAttendantId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Team A", result.get(0).getName());
        assertEquals("Team B", result.get(1).getName());
    }

    @Test
    void testFindByRotation() {
        // Arrange
        Long stationId = 1L;
        Long shiftRotationId = 100L;
        ShiftRotationDto currentRotation = ShiftRotationDto.builder().build();
        currentRotation.setId(shiftRotationId);

        PumpAttendantTeamDto teamDto = PumpAttendantTeamDto.builder()
                .id(1L)
                .name("Team A")
                .shiftRotationId(shiftRotationId)
                .build();

        List<PumpAttendantTeamDto> expectedTeams = Collections.singletonList(teamDto);

        when(shiftRotationService.findValidRotation(stationId, LocalDate.now())).thenReturn(currentRotation);
        when(dao.findByStationAndRotation(stationId, shiftRotationId)).thenReturn(expectedTeams);

        // Act
        List<PumpAttendantTeamDto> result = pumpAttendantTeamService.findByRotation(stationId, shiftRotationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Team A", result.get(0).getName());
    }
    @Test
    void testUpdateTeam_NonExistentTeam() {
        // Arrange
        Long stationId = 1L;
        Long teamId = 1L;
        PumpAttendantTeamDto updateTeamDto = PumpAttendantTeamDto.builder().id(teamId).build();

        when(dao.findById(teamId)).thenReturn(Optional.empty()); // Team does not exist

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class,
                () -> pumpAttendantTeamService.updateTeam(stationId, updateTeamDto, request));
        assertEquals("PumpAttendantTeam not found", exception.getMessage());
    }
    @Test
    void testUpdateTeam_PumpAttendantAlreadyInAnotherTeam() {
        // Arrange
        Long stationId = 1L;
        Long pumpAttendantId = 10L;
        Long shiftRotationId = 100L;
        Long teamId = 1L;

        User mockUser = new User(); // Mock user for authentication
        mockUser.setUsername("admin");

        // Mock required team and station objects
        StationDto stationDto = StationDto.builder().build();
        stationDto.setId(stationId);
        stationDto.setName("Station A");
        stationDto.setCustomerAccountName("Customer X");

        PumpAttendantDto pumpAttendantDto = PumpAttendantDto.builder()
                .id(pumpAttendantId)
                .firstName("John")
                .build(); // Ensure the PumpAttendant object has a name

        AffectedPumpAttendantDto affectedPumpAttendant = AffectedPumpAttendantDto.builder()
                .pumpAttendantId(pumpAttendantId)
                .pumpAttendant(pumpAttendantDto) // Set the proper PumpAttendant object
                .build();

        PumpAttendantTeamDto existingTeamDto = PumpAttendantTeamDto.builder()
                .id(teamId)
                .stationId(stationId)
                .shiftRotationId(shiftRotationId)
                .affectedPumpAttendant(Set.of(affectedPumpAttendant))
                .build();

        // Setting up the mocks
        when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
        when(dao.findById(teamId)).thenReturn(Optional.of(existingTeamDto));

        // This team already contains this pump attendant
        PumpAttendantTeamDto anotherTeam = PumpAttendantTeamDto.builder().id(2L).name("Team B").build();
        List<PumpAttendantTeamDto> existingTeams = List.of(anotherTeam); // Mock existing teams

        when(pumpAttendantTeamService.findTeamsByRotationAndPumpAttendant(shiftRotationId, pumpAttendantId))
                .thenReturn(existingTeams);

        // Ensure pumpAttendantDao returns the mocked pump attendant
        when(pumpAttendantDao.findById(pumpAttendantId)).thenReturn(Optional.of(pumpAttendantDto));

        // Set the security context
        SecurityContextHolder.setContext(Mockito.mock(SecurityContext.class));
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(Mockito.mock(Authentication.class));
        lenient().when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(mockUser);

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class,
                () -> pumpAttendantTeamService.updateTeam(stationId, existingTeamDto, request));

        // Log the exception message to understand what it is
        System.out.println("Caught exception: " + exception.getMessage());

        // Update assertions based on the actual message
        assertTrue(exception.getMessage().contains("PumpAttendant: John"));
        assertTrue(exception.getMessage().contains("is already affected to another team(s) for this rotation"));
    }
    @Test
    void testFindByRotation_WithSpecificShiftRotationId() {
        // Arrange
        Long stationId = 1L;
        Long shiftRotationId = 100L;

        ShiftRotationDto currentRotation = ShiftRotationDto.builder()
                .id(shiftRotationId)
                .build();

        PumpAttendantTeamDto teamDto = PumpAttendantTeamDto.builder()
                .id(1L)
                .name("Team A")
                .shiftRotationId(shiftRotationId)
                .build();

        List<PumpAttendantTeamDto> expectedTeams = Collections.singletonList(teamDto);

        // Mock the current rotation to return the shiftRotationId specified
        when(shiftRotationService.findValidRotation(stationId, LocalDate.now())).thenReturn(currentRotation);
        when(dao.findByStationAndRotation(stationId, shiftRotationId)).thenReturn(expectedTeams);

        // Act
        List<PumpAttendantTeamDto> result = pumpAttendantTeamService.findByRotation(stationId, shiftRotationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Team A", result.get(0).getName());
        Mockito.verify(dao).findByStationAndRotation(stationId, shiftRotationId);
    }

    @Test
    void testFindByRotation_WithNullShiftRotationId() {
        // Arrange
        Long stationId = 1L;
        Long shiftRotationId = null; // Null shift rotation ID

        Long currentRotationId = 200L; // This will be the current valid rotation ID.
        ShiftRotationDto currentRotation = ShiftRotationDto.builder()
                .id(currentRotationId)
                .build();

        PumpAttendantTeamDto teamDto = PumpAttendantTeamDto.builder()
                .id(1L)
                .name("Team B")
                .shiftRotationId(currentRotationId)
                .build();

        List<PumpAttendantTeamDto> expectedTeams = Collections.singletonList(teamDto);

        // Mock to return the current shift rotation
        when(shiftRotationService.findValidRotation(stationId, LocalDate.now())).thenReturn(currentRotation);
        when(dao.findByStationAndRotation(stationId, currentRotationId)).thenReturn(expectedTeams);

        // Act
        List<PumpAttendantTeamDto> result = pumpAttendantTeamService.findByRotation(stationId, shiftRotationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Team B", result.get(0).getName());
        Mockito.verify(dao).findByStationAndRotation(stationId, currentRotationId); // Verify dao method called with current id
    }
}