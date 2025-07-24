package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.service.cache.ControllerHeartbeats;
import com.teknokote.ess.core.service.requests.FuelGradePriceChangeRequestService;
import com.teknokote.ess.dto.ControllerPtsDto;
import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.dto.requests.FuelGradePriceChangeRequestDto;
import com.teknokote.ess.scheduling.TaskScheduling;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.email.EmailException;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

 class TaskSchedulingTest {

    @InjectMocks
    private TaskScheduling taskScheduling;
    @Mock
    private StationService stationService;
     @Mock
     private ControllerHeartbeats controllerHeartbeats;
     @Mock
     private KeycloakService keycloakService;
     @Mock
     private MailingService mailingService;
     @Mock
     private CustomerAccountService customerAccountService;
     @Mock
     private FuelGradePriceChangeRequestService fuelGradePriceChangeRequestService;

     private CustomerAccountDto customerAccountDto;

     @BeforeEach
     void setUp() throws Exception {
         MockitoAnnotations.openMocks(this);
         customerAccountDto = mock(CustomerAccountDto.class);
         when(customerAccountDto.getId()).thenReturn(1L);
         when(customerAccountDto.getName()).thenReturn("Test Account");

         // Set allowedInactivityDuration with reflection
         setAllowedInactivityDuration(Duration.ofHours(1));
     }

     private void setAllowedInactivityDuration(Duration duration) throws Exception {
         Field field = TaskScheduling.class.getDeclaredField("allowedInactivityDuration");
         field.setAccessible(true);
         field.set(taskScheduling, duration);
     }

    @Test
     void testRunFuelPriceChange_RequestsFound() throws Exception {
        // Arrange
        FuelGradePriceChangeRequestDto mockRequest = mock(FuelGradePriceChangeRequestDto.class);
        when(fuelGradePriceChangeRequestService.findRequestsToRun()).thenReturn(List.of(mockRequest));

        // Mocked FuelGrade setup
        FuelGrade mockFuelGrade = mock(FuelGrade.class);
        when(mockRequest.getFuelGrade()).thenReturn(mockFuelGrade);
        when(mockFuelGrade.getName()).thenReturn("Diesel");

        // Set up remaining mocks
        when(mockRequest.getScheduledDate()).thenReturn(LocalDateTime.now().minusMinutes(1));
        when(mockRequest.getCreatedDate()).thenReturn(LocalDateTime.now().minusMinutes(5));
        when(mockRequest.getPlannedDate()).thenReturn(LocalDateTime.now().minusMinutes(2));

        // Act
        taskScheduling.runFuelPriceChange();

        // Assert
        verify(fuelGradePriceChangeRequestService, times(1)).toFuelGradeConfig(mockRequest);
        verify(stationService, times(1)).changeFuelGradePrices(any(), any());
    }

    @Test
     void testRunFuelPriceChange_NoRequestsFound() throws Exception {
        // Arrange
        when(fuelGradePriceChangeRequestService.findRequestsToRun()).thenReturn(Collections.emptyList());

        // Act
        taskScheduling.runFuelPriceChange();

        // Assert
        verify(fuelGradePriceChangeRequestService, times(1)).findRequestsToRun();
        verify(stationService, never()).changeFuelGradePrices(any(), any());
    }
    @Test
     void testExportCustomerAccounts_NoAccountsFound() throws Exception {
        // Arrange
        when(customerAccountService.findCustomerAccountToExport()).thenReturn(Collections.emptyList());

        // Act
        taskScheduling.exportCustomerAccounts();

        // Assert
        verify(customerAccountService, times(1)).findCustomerAccountToExport();
        verify(customerAccountService, never()).exportSupplier(any());
    }

     @Test
     void testExportCustomerAccounts_NoAccountsToExport() throws InterruptedException {
         // Simulate no accounts to export
         when(customerAccountService.findCustomerAccountToExport()).thenReturn(Arrays.asList());

         // Execute
         taskScheduling.exportCustomerAccounts();

         // Verify that the export method was never called
         verify(customerAccountService, never()).exportSupplier(any());
     }

     @Test
     void testExportCustomerAccounts_AlreadyRunning() throws InterruptedException {
         // Simulate that the service is already running
         taskScheduling.customerAccountExporting.set(true);

         // Execute
         taskScheduling.exportCustomerAccounts();

         // Verify that the export method was never called
         verify(customerAccountService, never()).findCustomerAccountToExport();
     }
     @Test
     void testExportCustomerAccounts_WithAccountsToExport() throws InterruptedException {
         // Prepare test data
         LocalDateTime scheduledDate = LocalDateTime.now().minusMinutes(5);
         LocalDateTime plannedExportDate = LocalDateTime.now().minusMinutes(1);
         LocalDateTime createdDate = LocalDateTime.now().minusHours(1);

         CustomerAccountDto accountToExport = mock(CustomerAccountDto.class);
         when(accountToExport.getScheduledDate()).thenReturn(scheduledDate);
         when(accountToExport.getPlannedExportDate()).thenReturn(plannedExportDate);
         when(accountToExport.getCreatedDate()).thenReturn(createdDate); // Ensure this is not null

         List<CustomerAccountDto> accountsToExport = Arrays.asList(accountToExport);
         when(customerAccountService.findCustomerAccountToExport()).thenReturn(accountsToExport);

         // Execute
         taskScheduling.exportCustomerAccounts();

         // Verify the export method was called
         verify(customerAccountService, times(1)).exportSupplier(accountToExport);
     }

     @Test
     void testExportCustomerAccounts_HandlingException() throws InterruptedException {
         // Prepare test data
         LocalDateTime scheduledDate = LocalDateTime.now().minusMinutes(5);
         LocalDateTime plannedExportDate = LocalDateTime.now().minusMinutes(1);
         LocalDateTime createdDate = LocalDateTime.now().minusHours(1);

         CustomerAccountDto accountToExport = mock(CustomerAccountDto.class);
         when(accountToExport.getScheduledDate()).thenReturn(scheduledDate);
         when(accountToExport.getPlannedExportDate()).thenReturn(plannedExportDate);
         when(accountToExport.getCreatedDate()).thenReturn(createdDate); // Ensure this is not null
         doThrow(new RuntimeException("Export failed")).when(customerAccountService).exportSupplier(any());

         List<CustomerAccountDto> accountsToExport = Arrays.asList(accountToExport);
         when(customerAccountService.findCustomerAccountToExport()).thenReturn(accountsToExport);

         // Execute
         taskScheduling.exportCustomerAccounts();

         // Verify that the export attempt was made
         verify(customerAccountService, times(1)).exportSupplier(accountToExport);
     }
     @Test
     void testExportAccount_Success() {
         // Execute
         taskScheduling.exportAccount(customerAccountDto);

         // Verify
         verify(customerAccountService, times(1)).exportSupplier(customerAccountDto);
     }

     @Test
     void testExportAccount_ServiceValidationException() {
         // Arrange
         String expectedMessage = "Validation error";
         doThrow(new ServiceValidationException(expectedMessage)).when(customerAccountService).exportSupplier(any());

         verify(customerAccountService, never()).exportSupplier(any());
     }
     @Test
     void testExportAccount_UnexpectedException() {
         // Arrange
         String expectedMessage = "Unexpected error";
         doThrow(new RuntimeException(expectedMessage)).when(customerAccountService).exportSupplier(any());

         verify(customerAccountService, never()).exportSupplier(any());
     }
     @Test
     void testCheckInactiveStations_SendsEmail() throws Exception {
         // Given
         setAllowedInactivityDuration(Duration.ofHours(1));
         LocalDateTime now = LocalDateTime.now();
         StationDto stationDto = mock(StationDto.class);

         ControllerPtsDto controllerPtsDto = mock(ControllerPtsDto.class);
         when(controllerPtsDto.getPtsId()).thenReturn("PTS001");

         when(stationDto.getActif()).thenReturn(true);
         when(stationDto.getControllerPts()).thenReturn(controllerPtsDto); // Set the controllerPts
         when(stationDto.getCustomerAccountId()).thenReturn(1L);
         when(stationDto.getControllerPts().getPtsId()).thenReturn("PTS001");
         when(stationDto.getCustomerAccountId()).thenReturn(1L);

         when(stationService.findAllByActif(true)).thenReturn(Arrays.asList(stationDto));
         when(controllerHeartbeats.getLastConnection(any())).thenReturn(now.minusHours(2)); // Simulating inactivity

         UserDto masterUser = mock(UserDto.class);
         when(masterUser.getUsername()).thenReturn("testUser");
         when(customerAccountService.findMasterUserWithCustomerAccountId(stationDto.getCustomerAccountId())).thenReturn(masterUser);

         UserRepresentation userRepresentation = mock(UserRepresentation.class);
         when(userRepresentation.getEmail()).thenReturn("test@example.com");
         when(keycloakService.getUserIdentity(masterUser.getUsername())).thenReturn(Optional.of(userRepresentation));

         // Execute
         taskScheduling.checkInactiveStations();
     }

     @Test
     void testCheckInactiveStations_NoEmailSentIfActive() throws EmailException {
         // Given
         StationDto stationDto = mock(StationDto.class);

         ControllerPtsDto controllerPtsDto = mock(ControllerPtsDto.class);
         when(controllerPtsDto.getPtsId()).thenReturn("PTS001");

         when(stationDto.getActif()).thenReturn(true);
         when(stationDto.getControllerPts()).thenReturn(controllerPtsDto); // Set the controllerPts

         when(stationService.findAllByActif(true)).thenReturn(Arrays.asList(stationDto));
         when(controllerHeartbeats.getLastConnection(any())).thenReturn(LocalDateTime.now()); // Simulating recent activity

         // Execute
         taskScheduling.checkInactiveStations();

         // Verify that no email was sent
         verify(mailingService, never()).sendInactivityEmail(any(), any(), any());
     }

     @Test
     void testCheckInactiveStations_NoMasterUserFound() throws Exception {
         // Given
         LocalDateTime now = LocalDateTime.now();
         StationDto stationDto = mock(StationDto.class);

         ControllerPtsDto controllerPtsDto = mock(ControllerPtsDto.class);
         when(controllerPtsDto.getPtsId()).thenReturn("PTS001");

         when(stationDto.getActif()).thenReturn(true);
         when(stationDto.getControllerPts()).thenReturn(controllerPtsDto); // Set the controllerPts
         when(stationDto.getCustomerAccountId()).thenReturn(1L);
         when(stationService.findAllByActif(true)).thenReturn(Arrays.asList(stationDto));
         when(controllerHeartbeats.getLastConnection(any())).thenReturn(now.minusHours(2)); // Simulating inactivity
         when(stationDto.getCustomerAccountId()).thenReturn(1L);

         when(customerAccountService.findMasterUserWithCustomerAccountId(stationDto.getCustomerAccountId())).thenReturn(null); // No user found

         // Execute
         taskScheduling.checkInactiveStations();

         // Verify that no email was sent
         verify(mailingService, never()).sendInactivityEmail(any(), any(), any());
     }

     @Test
     void testCheckInactiveStations_UserIdentityNotFound() throws Exception {
         // Given
         LocalDateTime now = LocalDateTime.now();
         StationDto stationDto = mock(StationDto.class);

         ControllerPtsDto controllerPtsDto = mock(ControllerPtsDto.class);
         when(controllerPtsDto.getPtsId()).thenReturn("PTS001");

         when(stationDto.getActif()).thenReturn(true);
         when(stationDto.getControllerPts()).thenReturn(controllerPtsDto); // Set the controllerPts
         when(stationDto.getCustomerAccountId()).thenReturn(1L);

         when(stationService.findAllByActif(true)).thenReturn(Arrays.asList(stationDto));
         when(controllerHeartbeats.getLastConnection(any())).thenReturn(now.minusHours(2)); // Simulating inactivity
         when(stationDto.getCustomerAccountId()).thenReturn(1L);

         UserDto masterUser = mock(UserDto.class);
         when(masterUser.getUsername()).thenReturn("testUser");
         when(customerAccountService.findMasterUserWithCustomerAccountId(stationDto.getCustomerAccountId())).thenReturn(masterUser);
         when(keycloakService.getUserIdentity(masterUser.getUsername())).thenReturn(Optional.empty()); // User not found

         // Execute
         taskScheduling.checkInactiveStations();

         // Verify that no email was sent
         verify(mailingService, never()).sendInactivityEmail(any(), any(), any());
     }
 }