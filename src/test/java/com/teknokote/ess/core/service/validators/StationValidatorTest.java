package com.teknokote.ess.core.service.validators;

import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.core.dao.StationDao;
import com.teknokote.ess.core.service.impl.ControllerService;
import com.teknokote.ess.core.service.impl.CountryService;
import com.teknokote.ess.core.service.impl.validators.StationValidator;
import com.teknokote.ess.dto.ControllerPtsDto;
import com.teknokote.ess.dto.CountryDto;
import com.teknokote.ess.dto.StationDto;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class StationValidatorTest {
    private ESSValidator<StationDto> validator;
    private ControllerService controllerService;
    private CountryService countryService;
    private StationDao stationDao;

    @BeforeEach
     void init() {
        this.controllerService = mock(ControllerService.class);
        this.countryService = mock(CountryService.class);
        this.stationDao = mock(StationDao.class);
        validator = new StationValidator(controllerService, countryService, null, stationDao);
    }

    @Test
    void givenEmptyBeanShouldRaiseStructuralErrors() {
        StationDto stationDto = StationDto.builder().build();

        final ESSValidationResult validationResult = validator.validateOnCreate(stationDto);
        assertTrue(validationResult.hasErrors());
        final String errorMessage = validationResult.getMessage();
        log.error(errorMessage);
        assertEquals(4, validationResult.getErrors().size());
        Assertions.assertThat(errorMessage).contains("name");
    }

    @Test
    void givenControllerAbsentShouldRaiseControllerShouldBeNotEmpty() {
        StationDto stationDto = StationDto.builder().name("station 1").customerAccountId(1L).country(CountryDto.builder().build()).build();

        final ESSValidationResult validationResult = validator.validateOnCreate(stationDto);
        final String errorMessage = validationResult.getMessage();
        log.error(errorMessage);
        assertEquals(3, validationResult.getErrors().size());
        Assertions.assertThat(errorMessage).contains("contrôleur");
    }

    @Test
    void givenNecessaryDataShouldBeOk() {
        StationDto stationDto = StationDto.builder().name("station 1")
                .controllerPts(ControllerPtsDto.builder().build())
                .customerAccountId(1L)
                .countryId(1L)
                .build();

        when(controllerService.findControllerDto(any())).thenReturn(Optional.empty());
        when(countryService.findById(any())).thenReturn(Optional.of(CountryDto.builder().id(1L).build()));
        final ESSValidationResult validationResult = validator.validateOnCreate(stationDto);
        assertTrue(validationResult.hasNoErrors());
    }

    @Test
    void givenExistingControllerShouldRaiseExistingontrollerError() {
        StationDto stationDto = StationDto.builder().name("station 1")
                .controllerPts(ControllerPtsDto.builder().build())
                .customerAccountId(1L)
                .countryId(1L)
                .build();

        when(controllerService.findControllerDto(any())).thenReturn(Optional.of(ControllerPtsDto.builder().ptsId("11111").build()));
        when(countryService.findById(any())).thenReturn(Optional.of(CountryDto.builder().id(1L).build()));
        final ESSValidationResult validationResult = validator.validateOnCreate(stationDto);
        final String errorMessage = validationResult.getMessage();
        log.error(errorMessage);
        assertEquals(1, validationResult.getErrors().size());
        Assertions.assertThat(validationResult.getMessage()).contains(StationValidator.ERROR_EXISTING_CONTROLLER_WITH_SAME_PTS_ID);
    }
}
