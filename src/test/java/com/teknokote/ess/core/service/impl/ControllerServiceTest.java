package com.teknokote.ess.core.service.impl;

import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.ess.core.dao.ControllerDao;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.repository.ControllePtsRepository;
import com.teknokote.ess.core.service.impl.validators.ControllerValidator;
import com.teknokote.ess.dto.ControllerPtsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControllerServiceTest {

    @Mock
    private ControllerDao dao;
    @Mock
    private ControllePtsRepository controllePtsRepository;
    @Mock
    private ESSValidationResult validationResult;
    @Mock
    private ControllerValidator validator;

    @InjectMocks
    private ControllerService controllerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(dao.getRepository()).thenReturn(controllePtsRepository);
    }
    @Test
    void testRepositorySave() {
        // Arrange
        ControllerPts entity = new ControllerPts();

        when(controllePtsRepository.save(any(ControllerPts.class))).thenReturn(entity);

        // Act
        ControllerPts savedEntity = controllePtsRepository.save(entity);

        // Assert
        assertNotNull(savedEntity);
        // Additional assertions based on expected values
        verify(controllePtsRepository, times(1)).save(entity);
    }
    @Test
    void testFindControllerHandlesDaoException() {
        // Arrange
        String ptsId = "pts123";
        when(dao.findControllerPtsById(ptsId)).thenThrow(new IllegalStateException("DAO error"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            controllerService.findControllerDto(ptsId);
        });

        assertEquals("DAO error", exception.getMessage());
        verify(dao, times(1)).findControllerPtsById(ptsId);
    }
    @Test
    void testFindControllerDtoReturnsPresentOptional() {
        // Arrange
        String ptsId = "pts123";
        ControllerPtsDto dto = ControllerPtsDto.builder().build();
        when(dao.findControllerPtsById(ptsId)).thenReturn(Optional.of(dto));

        // Act
        Optional<ControllerPtsDto> result = controllerService.findControllerDto(ptsId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
        verify(dao, times(1)).findControllerPtsById(ptsId);
    }

    @Test
    void testFindControllerDtoReturnsEmptyOptional() {
        // Arrange
        String ptsId = "pts123";
        when(dao.findControllerPtsById(ptsId)).thenReturn(Optional.empty());

        // Act
        Optional<ControllerPtsDto> result = controllerService.findControllerDto(ptsId);

        // Assert
        assertFalse(result.isPresent());
        verify(dao, times(1)).findControllerPtsById(ptsId);
    }

    @Test
    void testFindControllerReturnsControllerPts() {
        // Arrange
        String ptsId = "pts123";
        ControllerPts controller = new ControllerPts();
        when(controllePtsRepository.findControllerPtsById(ptsId)).thenReturn(Optional.of(controller));

        // Act
        ControllerPts result = controllerService.findController(ptsId);

        // Assert
        assertNotNull(result);
        assertEquals(controller, result);
        verify(controllePtsRepository, times(1)).findControllerPtsById(ptsId);
    }

    @Test
    void testFindControllerReturnsNull() {
        // Arrange
        String ptsId = "pts123";
        when(controllePtsRepository.findControllerPtsById(ptsId)).thenReturn(Optional.empty());

        // Act
        ControllerPts result = controllerService.findController(ptsId);

        // Assert
        assertNull(result);
        verify(controllePtsRepository, times(1)).findControllerPtsById(ptsId);
    }

    @Test
    void testFindControllerWithNullPtsId() {
        // Arrange
        String ptsId = null;

        // Act
        ControllerPts result = controllerService.findController(ptsId);

        // Assert
        assertNull(result, "Expected null for null ptsId");
    }
    @Test
    void testFindControllerHandlesUnexpectedDatabaseBehavior() {
        // Arrange
        String ptsId = "pts123";
        // We can set up the `controllePtsRepository` mock to return an empty `Optional`.
        when(controllePtsRepository.findControllerPtsById(ptsId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            controllerService.findController(ptsId);
        });

        assertEquals("Database error", exception.getMessage());
        verify(controllePtsRepository, times(1)).findControllerPtsById(ptsId);
    }
}