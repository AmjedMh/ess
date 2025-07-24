package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.core.model.FirmwareInformation;
import com.teknokote.ess.core.repository.FirmwareInformationRepository;
import com.teknokote.ess.dto.FirmwareInformationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class FirmwareInformationServiceTest {

    @Mock
    private FirmwareInformationRepository firmwareInformationRepository;
    @Mock
    private ControllerService controllerService;
    @InjectMocks
    private FirmwareInformationService firmwareInformationService;


    @Test
    void testUpdateFirmwareInformation_NewVersion() {
        // Given
        String ptsId = "0027003A3438510935383135";
        String firmwareVersion = "25-01-06T23:59:14";
        boolean isFirmwareVersionSupported = true;

        // Mock repository call
        when(firmwareInformationRepository.findAllByPtsId(ptsId)).thenReturn(Arrays.asList());
        when(controllerService.findController(ptsId)).thenReturn(null); // Mocked controller

        // When
        firmwareInformationService.updateFirmwareInformation(ptsId, firmwareVersion, isFirmwareVersionSupported);

        // Then
        verify(firmwareInformationRepository, times(1)).save(any(FirmwareInformation.class));
    }

    @Test
    void testUpdateFirmwareInformation_ExistingVersion() {
        // Given
        String ptsId = "0027003A3438510935383135";
        String firmwareVersion = "25-01-06T23:59:14";
        boolean isFirmwareVersionSupported = true;

        FirmwareInformation existingFirmware = FirmwareInformation.builder()
                .ptsId(ptsId)
                .dateTime(firmwareVersion)
                .modificationDate(LocalDateTime.now().minusDays(1))
                .build();

        // Mock repository calls
        when(firmwareInformationRepository.findAllByPtsId(ptsId)).thenReturn(Arrays.asList(existingFirmware));

        // When
        firmwareInformationService.updateFirmwareInformation(ptsId, firmwareVersion, isFirmwareVersionSupported);

        // Then
        verify(firmwareInformationRepository, times(1)).save(existingFirmware);
    }

    @Test
    void testFirmwareVersion_FoundVersion() {
        // Given
        String ptsId = "0027003A3438510935383135";
        String firmwareVersion = "25-01-06T23:59:14";

        // Mock repository call
        when(firmwareInformationRepository.getLastModifiedFirmwareInformationVersionByPtsId(ptsId))
                .thenReturn(Optional.of(firmwareVersion));

        // When
        String result = firmwareInformationService.firmwareVersion(ptsId);

        // Then
        assertEquals(firmwareVersion, result);
    }

    @Test
    void testFirmwareVersion_NoVersion() {
        // Given
        String ptsId = "0027003A3438510935383135";

        // Mock repository call
        when(firmwareInformationRepository.getLastModifiedFirmwareInformationVersionByPtsId(ptsId))
                .thenReturn(Optional.empty());

        // When
        String result = firmwareInformationService.firmwareVersion(ptsId);

        // Then
        assertEquals("-", result);
    }

    @Test
    void testCurrentFirmwareVersion_FoundVersion() {
        // Given
        String ptsId = "0027003A3438510935383135";
        FirmwareInformation firmwareInformation = FirmwareInformation.builder()
                .ptsId(ptsId)
                .dateTime("25-01-06T23:59:14")
                .modificationDate(LocalDateTime.now())
                .build();

        // Mock repository call
        when(firmwareInformationRepository.getLastModifiedFirmwareInformationVersion(ptsId))
                .thenReturn(Optional.of(firmwareInformation));

        // When
        FirmwareInformationDto result = firmwareInformationService.currentFirmwareVersion(ptsId);

        // Then
        assertNotNull(result);
        assertEquals(firmwareInformation.getDateTime(), result.getDateTime());
        assertEquals(firmwareInformation.getPtsId(), result.getPtsId());
    }

    @Test
    void testCurrentFirmwareVersion_NoVersion() {
        // Given
        String ptsId = "0027003A3438510935383135";

        // Mock repository call
        when(firmwareInformationRepository.getLastModifiedFirmwareInformationVersion(ptsId))
                .thenReturn(Optional.empty());

        // When
        FirmwareInformationDto result = firmwareInformationService.currentFirmwareVersion(ptsId);

        // Then
        assertNull(result);
    }

    @Test
    void testUpdateFirmwareInformation_DuplicateVersion() {
        // Given
        String ptsId = "0027003A3438510935383135";
        String firmwareVersion = "25-01-06T23:59:14";
        boolean isFirmwareVersionSupported = true;

        FirmwareInformation existingFirmware = FirmwareInformation.builder()
                .ptsId(ptsId)
                .dateTime(firmwareVersion)
                .modificationDate(LocalDateTime.now())
                .build();

        // Mock repository call to simulate duplicate version
        when(firmwareInformationRepository.findAllByPtsId(ptsId)).thenReturn(Arrays.asList(existingFirmware, existingFirmware));

        // When & Then
        assertThrows(ServiceValidationException.class, () -> {
            firmwareInformationService.updateFirmwareInformation(ptsId, firmwareVersion, isFirmwareVersionSupported);
        });
    }
    @Test
    void testUpdateFirmwareInformation_ExistingVersionAndLatestModification() {
        // Given
        String ptsId = "0027003A3438510935383135";
        String firmwareVersion = "25-01-06T23:59:14";
        boolean isFirmwareVersionSupported = true;

        // Create an existing FirmwareInformation that matches the same firmwareVersion
        FirmwareInformation existingFirmware = FirmwareInformation.builder()
                .ptsId(ptsId)
                .dateTime(firmwareVersion)
                .modificationDate(LocalDateTime.now()) // Mark as the latest modification date
                .build();

        // Mock repository calls
        when(firmwareInformationRepository.findAllByPtsId(ptsId)).thenReturn(Arrays.asList(existingFirmware));
        when(firmwareInformationRepository.save(any(FirmwareInformation.class))).thenReturn(existingFirmware); // Ensure this is actually called in the service method

        // We expect no new save call here, just the date should be updated in the existing firmware
        firmwareInformationService.updateFirmwareInformation(ptsId, firmwareVersion, isFirmwareVersionSupported);

        // Then
        verify(firmwareInformationRepository, times(1)).save(any(FirmwareInformation.class));

        // Verify the modification date was updated
        assertEquals(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS), existingFirmware.getModificationDate().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
    }
}
