package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.ess.core.dao.PumpAttendantDao;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.impl.validators.PumpAttendantValidator;
import com.teknokote.ess.core.service.impl.validators.StationValidator;
import com.teknokote.ess.core.service.organization.PumpAttendantTeamService;
import com.teknokote.ess.dto.PumpAttendantDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.http.logger.EntityActionEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PumpAttendantServiceTest {
    @InjectMocks
    private PumpAttendantServiceImpl pumpAttendantService;
    @Mock
    private PumpAttendantDao pumpAttendantDao;
    @Mock
    private StationService stationService;
    @Mock
    private StationValidator stationValidator;
    @Mock
    private PumpAttendantValidator pumpAttendantValidator;
    @Mock
    private ESSValidationResult validationResult;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private PumpAttendantTeamService pumpAttendantTeamService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private PumpAttendantDto pumpAttendantDto;
    @Mock
    private StationDto stationDto;
    @TempDir
    Path tempDir;
    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        ReflectionTestUtils.setField(pumpAttendantService, "photoStoragePath", tempDir.toString());
    }

    @Test
    void testAddPumpAttendant_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Long stationId = 1L;
        User mockUser = new User();
        PumpAttendantDto savedPumpAttendantDto = PumpAttendantDto.builder().build();
        when(pumpAttendantValidator.validateOnCreate(pumpAttendantDto)).thenReturn(validationResult);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
        when(stationValidator.validateOnAddPumpAttendant(pumpAttendantDto)).thenReturn(validationResult);
        when(pumpAttendantDto.getFirstName()).thenReturn("John");
        when(stationDto.getName()).thenReturn("Station A");
        when(stationDto.getCustomerAccountName()).thenReturn("Customer 1");
        MultipartFile photoFile = new MockMultipartFile(
                "photo.jpg", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3, 4}
        );
        when(pumpAttendantService.create(pumpAttendantDto)).thenReturn(savedPumpAttendantDto);

        // Act
        PumpAttendantDto result = pumpAttendantService.addPumpAttendant(stationId, pumpAttendantDto, photoFile, request);

        // Assert
        assertNotNull(result);
        verify(stationService).checkedFindById(stationId);
        verify(stationValidator).validateOnAddPumpAttendant(pumpAttendantDto);
        verify(eventPublisher).publishEvent(any(EntityActionEvent.class));
    }

    @Test
    void testAddPumpAttendant_ValidationFails() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Long stationId = 1L;
        when(stationValidator.validateOnAddPumpAttendant(pumpAttendantDto)).thenReturn(validationResult);

        when(validationResult.hasErrors()).thenReturn(true);
        // Act & Assert
        assertThrows(ServiceValidationException.class,
                () -> pumpAttendantService.addPumpAttendant(stationId, pumpAttendantDto, null, request));

    }

    @Test
    void testUpdatePumpAttendant_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Long stationId = 1L;
        User mockUser = new User();
        PumpAttendantDto existingPumpAttendant = PumpAttendantDto.builder().id(1L).firstName("John").build();
        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
        when(pumpAttendantService.findById(1L)).thenReturn(Optional.of(existingPumpAttendant));
        when(stationValidator.validateOnAddPumpAttendant(existingPumpAttendant)).thenReturn(validationResult);
        when(pumpAttendantValidator.validateOnUpdate(any(PumpAttendantDto.class))).thenReturn(validationResult);
        when(stationDto.getName()).thenReturn("Station A");
        when(stationDto.getCustomerAccountName()).thenReturn("Customer 1");
        when(pumpAttendantService.getDao().update(existingPumpAttendant)).thenReturn(existingPumpAttendant);

        // Act
        PumpAttendantDto result = pumpAttendantService.updatePumpAttendant(stationId, existingPumpAttendant, null, request);

        // Assert
        assertNotNull(result);
        verify(stationService).checkedFindById(stationId);
        verify(stationValidator).validateOnAddPumpAttendant(existingPumpAttendant);
        verify(eventPublisher).publishEvent(any(EntityActionEvent.class));
    }

    @Test
    void testActivatePumpAttendant_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Long stationId = 1L;
        Long pumpAttendantId = 2L;
        User mockUser = new User();
        stationDto = StationDto.builder().build();
        stationDto.setName("Station A");
        stationDto.setCustomerAccountName("Customer 1");

        pumpAttendantDto = PumpAttendantDto.builder().build();
        pumpAttendantDto.setFirstName("John");

        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
        when(pumpAttendantTeamService.findByPumpAttendant(pumpAttendantId)).thenReturn(Collections.emptyList());
        when(pumpAttendantDao.activate(pumpAttendantId)).thenReturn(pumpAttendantDto);

        // Act
        PumpAttendantDto result = pumpAttendantService.activatePumpAttendant(stationId, pumpAttendantId, request);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(eventPublisher).publishEvent(any(EntityActionEvent.class));
    }

    @Test
    void testDeactivatePumpAttendant_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Long stationId = 1L;
        Long pumpAttendantId = 2L;
        User mockUser = new User();
        stationDto = StationDto.builder().build();
        stationDto.setName("Station A");
        stationDto.setCustomerAccountName("Customer 1");

        pumpAttendantDto = PumpAttendantDto.builder().build();
        pumpAttendantDto.setFirstName("John");

        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(stationService.checkedFindById(stationId)).thenReturn(stationDto);
        when(pumpAttendantTeamService.findByPumpAttendant(pumpAttendantId)).thenReturn(Collections.emptyList());
        when(pumpAttendantDao.deactivate(pumpAttendantId)).thenReturn(pumpAttendantDto);

        // Act
        PumpAttendantDto result = pumpAttendantService.deactivatePumpAttendant(stationId, pumpAttendantId, request);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(eventPublisher).publishEvent(any(EntityActionEvent.class));
    }

    @Test
    void testFindByStationAndTag_Success() {
        // Arrange
        Long stationId = 1L;
        String tag = "TAG123";
        PumpAttendantDto pumpAttendant = PumpAttendantDto.builder().build();
        pumpAttendant.setTag(tag);

        when(pumpAttendantDao.findByStationAndTag(stationId, tag)).thenReturn(Optional.of(pumpAttendant));

        // Act
        PumpAttendantDto result = pumpAttendantService.findByStationAndTag(stationId, tag);

        // Assert
        assertNotNull(result);
        assertEquals(tag, result.getTag());
    }

    @Test
    void testFindByStationAndTag_NotFound() {
        // Arrange
        Long stationId = 1L;
        String tag = "TAG123";

        when(pumpAttendantDao.findByStationAndTag(stationId, tag)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                pumpAttendantService.findByStationAndTag(stationId, tag));

        assertEquals("pump attendant with tag: TAG123 not found", exception.getMessage());
    }

    @Test
    void testFindPumpAttendantByStationAndTag_Success() {
        // Arrange
        Long stationId = 1L;
        String tag = "TAG123";
        PumpAttendantDto pumpAttendant = PumpAttendantDto.builder().build();
        pumpAttendant.setTag(tag);

        when(pumpAttendantDao.findPumpAttandantByStationAndTag(stationId, tag)).thenReturn(Optional.of(pumpAttendant));

        // Act
        Optional<PumpAttendantDto> result = pumpAttendantService.findPumpAttandantByStationAndTag(stationId, tag);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(tag, result.get().getTag());
    }

    @Test
    void testFindByStationAndMatricule_Success() {
        // Arrange
        Long stationId = 1L;
        String matricule = "MAT123";
        PumpAttendantDto pumpAttendant = PumpAttendantDto.builder().build();
        pumpAttendant.setMatricule(matricule);

        when(pumpAttendantDao.findByStationAndMatricule(stationId, matricule)).thenReturn(Optional.of(pumpAttendant));

        // Act
        PumpAttendantDto result = pumpAttendantService.findByStationAndMatricule(stationId, matricule);

        // Assert
        assertNotNull(result);
        assertEquals(matricule, result.getMatricule());
    }

    @Test
    void testFindByStationAndMatricule_NotFound() {
        // Arrange
        Long stationId = 1L;
        String matricule = "MAT123";

        when(pumpAttendantDao.findByStationAndMatricule(stationId, matricule)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                pumpAttendantService.findByStationAndMatricule(stationId, matricule));

        assertEquals("pump attendant with matricule: MAT123 not found", exception.getMessage());
    }

    @Test
    void testFindByIdAndStationId_Success() {
        // Arrange
        Long stationId = 1L;
        Long pumpAttendantId = 2L;
        PumpAttendantDto pumpAttendant = PumpAttendantDto.builder().build();
        pumpAttendant.setId(pumpAttendantId);
        pumpAttendant.setStationId(stationId);

        when(pumpAttendantService.findById(pumpAttendantId)).thenReturn(Optional.of(pumpAttendant));

        // Act
        PumpAttendantDto result = pumpAttendantService.findByIdAndStationId(pumpAttendantId, stationId);

        // Assert
        assertNotNull(result);
        assertEquals(stationId, result.getStationId());
    }

    @Test
    void testFindByIdAndStationId_StationMismatch() {
        // Arrange
        Long stationId = 1L;
        Long pumpAttendantId = 2L;
        PumpAttendantDto pumpAttendant = PumpAttendantDto.builder().build();
        pumpAttendant.setId(pumpAttendantId);
        pumpAttendant.setStationId(99L); // Different station
        when(pumpAttendantService.findById(2L)).thenReturn(Optional.of(pumpAttendant));

        // Act & Assert
        ServiceValidationException exception = assertThrows(ServiceValidationException.class, () ->
                pumpAttendantService.findByIdAndStationId(pumpAttendantId, stationId));

        assertEquals("No valid pump attendant on the specified station.", exception.getMessage());
    }
}
