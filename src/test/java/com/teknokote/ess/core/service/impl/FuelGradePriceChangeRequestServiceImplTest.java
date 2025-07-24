package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.core.dao.StationDao;
import com.teknokote.ess.core.dao.requests.FuelGradePriceChangeRequestDao;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.model.requests.EnumRequestStatus;
import com.teknokote.ess.core.service.impl.requests.FuelGradePriceChangeRequestServiceImpl;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.requests.FuelGradePriceChangeRequestDto;
import com.teknokote.ess.http.logger.EntityActionEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuelGradePriceChangeRequestServiceImplTest {
    @InjectMocks
    private FuelGradePriceChangeRequestServiceImpl service;
    @Mock
    private FuelGradePriceChangeRequestDao fuelGradePriceChangeRequestDao;
    @Mock
    private StationDao stationDao;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private ESSValidator<FuelGradePriceChangeRequestDto> validator;
    @Mock
    private ESSValidationResult validationResult;
    private FuelGradePriceChangeRequestDto requestDto;
    private FuelGradeConfigDto fuelGradeConfigDto;
    private final Long stationId = 1L;
    private final Long requestId = 10L;
    private StationDto stationDto;
    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setUsername("testuser");

        stationDto = StationDto.builder().build();
        stationDto.setId(stationId);
        stationDto.setName("Test Station");
        stationDto.setCustomerAccountName("Test Customer");

        FuelGrade fuelGrade = new FuelGrade();
        fuelGrade.setName("Diesel");

        requestDto = FuelGradePriceChangeRequestDto.builder().build();
        requestDto.setId(requestId);
        requestDto.setStationId(stationId);
        requestDto.setFuelGrade(fuelGrade);

        fuelGradeConfigDto = new FuelGradeConfigDto();
        fuelGradeConfigDto.setId(1L);
        fuelGradeConfigDto.setIdConf(1L);
        fuelGradeConfigDto.setName("Gasoil");
        fuelGradeConfigDto.setPrice(2.3);

        // Mock security context
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void findExecutedByStation_ShouldReturnListOfRequests() {
        // Arrange
        List<FuelGradePriceChangeRequestDto> expectedRequests = Collections.singletonList(requestDto);
        when(fuelGradePriceChangeRequestDao.findExecutedByStation(stationId)).thenReturn(expectedRequests);

        // Act
        List<FuelGradePriceChangeRequestDto> result = service.findExecutedByStation(stationId);

        // Assert
        assertEquals(expectedRequests, result);
        verify(fuelGradePriceChangeRequestDao).findExecutedByStation(stationId);
    }

    @Test
    void findPlannedByStation_ShouldReturnListOfRequests() {
        // Arrange
        List<FuelGradePriceChangeRequestDto> expectedRequests = Collections.singletonList(requestDto);
        when(fuelGradePriceChangeRequestDao.findPlannedByStation(stationId)).thenReturn(expectedRequests);

        // Act
        List<FuelGradePriceChangeRequestDto> result = service.findPlannedByStation(stationId);

        // Assert
        assertEquals(expectedRequests, result);
        verify(fuelGradePriceChangeRequestDao).findPlannedByStation(stationId);
    }

    @Test
    void findFuelGradePriceChangeRequestByStation_ShouldReturnRequest() {
        // Arrange
        when(fuelGradePriceChangeRequestDao.findByStation(stationId, requestId)).thenReturn(requestDto);

        // Act
        FuelGradePriceChangeRequestDto result = service.findFuelGradePriceChangeRequestByStation(stationId, requestId);

        // Assert
        assertEquals(requestDto, result);
        verify(fuelGradePriceChangeRequestDao).findByStation(stationId, requestId);
    }

    @Test
    void findRequestsToRun_ShouldReturnPlannedOrFailedRequests() {
        // Arrange
        List<FuelGradePriceChangeRequestDto> expectedRequests = Collections.singletonList(requestDto);
        when(fuelGradePriceChangeRequestDao.findAllByStatusAndReachedPlannedDate(
                List.of(EnumRequestStatus.PLANNED, EnumRequestStatus.FAILED))
        ).thenReturn(expectedRequests);

        // Act
        List<FuelGradePriceChangeRequestDto> result = service.findRequestsToRun();

        // Assert
        assertEquals(expectedRequests, result);
        verify(fuelGradePriceChangeRequestDao).findAllByStatusAndReachedPlannedDate(
                List.of(EnumRequestStatus.PLANNED, EnumRequestStatus.FAILED));
    }

    @Test
    void toFuelGradeConfig_ShouldConvertRequestToConfig() {
        // Arrange
        when(fuelGradePriceChangeRequestDao.toFuelGradeConfig(requestDto)).thenReturn(fuelGradeConfigDto);

        // Act
        FuelGradeConfigDto result = service.toFuelGradeConfig(requestDto);

        // Assert
        assertEquals(fuelGradeConfigDto, result);
        verify(fuelGradePriceChangeRequestDao).toFuelGradeConfig(requestDto);
    }

    @Test
    void updatePlannedFuelGradeRequest_ShouldUpdateRequestAndPublishEvent() {
        // Arrange
        when(stationDao.findById(stationId)).thenReturn(Optional.of(stationDto));

        // Mock the behavior of the validator
        when(validator.validateOnUpdate(any(FuelGradePriceChangeRequestDto.class))).thenReturn(validationResult);
        when(validationResult.hasErrors()).thenReturn(false);

        // Mock the update call on the DAO
        when(fuelGradePriceChangeRequestDao.update(any(FuelGradePriceChangeRequestDto.class))).thenReturn(requestDto);

        // Act
        FuelGradePriceChangeRequestDto result = service.updatePlannedFuelGradeRequest(stationId, requestDto, httpServletRequest);

        // Assert
        assertEquals(requestDto, result);
        verify(stationDao).findById(stationId);
        verify(validator).validateOnUpdate(requestDto);
        verify(fuelGradePriceChangeRequestDao).update(requestDto);
        verify(eventPublisher).publishEvent(any(EntityActionEvent.class));
    }

    @Test
    void updatePlannedFuelGradeRequest_WhenStationNotFound_ShouldThrowException() {
        // Arrange
        when(stationDao.findById(stationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            service.updatePlannedFuelGradeRequest(stationId, requestDto, httpServletRequest);
        });
        verify(stationDao).findById(stationId);
        verifyNoInteractions(validator, fuelGradePriceChangeRequestDao, eventPublisher);
    }
}