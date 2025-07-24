package com.teknokote.ess.scheduling;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.core.service.cache.ControllerHeartbeats;
import com.teknokote.ess.core.service.impl.CustomerAccountService;
import com.teknokote.ess.core.service.impl.KeycloakService;
import com.teknokote.ess.core.service.impl.MailingService;
import com.teknokote.ess.core.service.impl.StationService;
import com.teknokote.ess.core.service.requests.FuelGradePriceChangeRequestService;
import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.dto.requests.FuelGradePriceChangeRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.email.EmailException;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@EnableScheduling
@Component
@Slf4j
public class TaskScheduling {
    @Autowired
    private StationService stationService;
    @Autowired
    private CustomerAccountService customerAccountService;
    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private MailingService mailingService;
    @Autowired
    private FuelGradePriceChangeRequestService fuelGradePriceChangeRequestService;
    private final AtomicBoolean fuelPriceChangeRunning = new AtomicBoolean(false);
    public final AtomicBoolean customerAccountExporting = new AtomicBoolean(false);
    @Autowired
    private ControllerHeartbeats controllerHeartbeats;
    private final Object object = new Object();
    @Value("${app.station.allowed-inactivity-duration}")
    private Duration allowedInactivityDuration;

    @Scheduled(cron = "0/30 * * * * *")
    public void runFuelPriceChange() throws InterruptedException {
        synchronized (object) {
            if (fuelPriceChangeRunning.get()) return;
            fuelPriceChangeRunning.set(true);
        }
        try (final TaskExecution ignored = TaskExecution.of(fuelPriceChangeRunning)) {
            log.info("**Launching Fuel Grade Price Change **");
            List<FuelGradePriceChangeRequestDto> requestsToRun = fuelGradePriceChangeRequestService.findRequestsToRun();
            if (requestsToRun.isEmpty()) {
                log.info("No fuel grade update requests found.");
            }
            requestsToRun.forEach(request -> {
                LocalDateTime now = LocalDateTime.now();

                // Calculate the time range between scheduledDate and createdDate
                Duration range = Duration.between(request.getScheduledDate(), request.getCreatedDate());

                // Calculate the expected execution time based on plannedDate
                LocalDateTime expectedExecutionTime = request.getPlannedDate().plus(range);
                log.info("Range Date: {}", request.getScheduledDate());
                // Log the calculated times for debugging
                log.info("Scheduled Date: {}", request.getScheduledDate());
                log.info("Created Date: {}", request.getCreatedDate());
                log.info("Planned Date: {}", request.getPlannedDate());
                log.info("Calculated Range: {}", range);
                log.info("Expected Execution Time: {}", expectedExecutionTime);

                // Check if the current time is within the expected execution time range
                if (!now.isBefore(expectedExecutionTime)) {
                    // Execute the fuel grade price change
                    runRequest(request);
                } else {
                    log.info("Not yet time to execute request for fuel grade price change.");
                }
            });
        } finally {
            synchronized (object) {
                fuelPriceChangeRunning.set(false);
            }
        }
    }

    private void runRequest(FuelGradePriceChangeRequestDto fuelGradePriceChangeRequest) {
        log.info("Updating fuel grade price:{} for station:{}", fuelGradePriceChangeRequest.getFuelGrade().getName(), fuelGradePriceChangeRequest.getStationId());
        fuelGradePriceChangeRequest.startTrial();
        try {
            stationService.changeFuelGradePrices(fuelGradePriceChangeRequest.getStationId(), fuelGradePriceChangeRequestService.toFuelGradeConfig(fuelGradePriceChangeRequest));
            fuelGradePriceChangeRequest.succeed();
            Double oldPrice = stationService.getFuelGradeByStationAndConfiguration(fuelGradePriceChangeRequest.getStationId(), fuelGradePriceChangeRequest.getFuelGrade().getIdConf()).getPrice();
            fuelGradePriceChangeRequest.setOldPrice(oldPrice);
            fuelGradePriceChangeRequestService.update(fuelGradePriceChangeRequest);
            log.info("Update fuel grade price:{} for station:{}:executed successfully", fuelGradePriceChangeRequest.getFuelGrade().getName(), fuelGradePriceChangeRequest.getStationId());
        } catch (Exception exception) {
            fuelGradePriceChangeRequest.fail();
            log.info("Update fuel grade price:{} for station:{}:FAILD! - Cause:{}", fuelGradePriceChangeRequest.getFuelGrade().getName(), fuelGradePriceChangeRequest.getStationId(), exception.getMessage());
        }
    }

    @Scheduled(cron = "0/30 * * * * *")
    public void exportCustomerAccounts() throws InterruptedException {
        synchronized (object) {
            if (customerAccountExporting.get()) return;
            customerAccountExporting.set(true);
        }

        try (final TaskExecution ignored = TaskExecution.of(customerAccountExporting)) {
            final List<CustomerAccountDto> accountsToExport = customerAccountService.findCustomerAccountToExport();
            if (!accountsToExport.isEmpty()) {
                accountsToExport.forEach(accountToExport ->{
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime scheduledExport = accountToExport.getScheduledDate();
                    Duration range = Duration.between(scheduledExport,accountToExport.getDateStatusChange() != null ? accountToExport.getDateStatusChange() : accountToExport.getCreatedDate());
                    // Calculate the expected execution time based on plannedExportDate
                    LocalDateTime expectedExecutionTime = accountToExport.getPlannedExportDate().plus(range);
                    if (!now.isBefore(expectedExecutionTime)) {
                        log.info("** Starting Customer Account Export **");
                        exportAccount(accountToExport);
                    }
            });
            }
        }
    }

    public void exportAccount(CustomerAccountDto customerAccountDto) {
        try {
            log.info("Exporting customer account:{}", customerAccountDto.getName());
            customerAccountService.exportSupplier(customerAccountDto);
            log.info("Customer account:{} exported successfully", customerAccountDto.getName());
        } catch (ServiceValidationException e) {
            log.error("Failed to export customer account:{} - Cause:{}", customerAccountDto.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error occurred while exporting customer account:{} - Cause:{}", customerAccountDto.getId(), e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void checkInactiveStations(){
        LocalDateTime now = LocalDateTime.now();
        List<StationDto> stations = stationService.findAllByActif(true);
        for (StationDto stationDto : stations) {
            LocalDateTime lastConnectionTime = controllerHeartbeats.getLastConnection(stationDto.getControllerPts().getPtsId());
            if (lastConnectionTime !=null && Duration.between(lastConnectionTime, now).compareTo(allowedInactivityDuration) > 0) {
                    // Send an email notification for inactive station
                    //sendInactivityEmail(stationDto,lastConnectionTime);
            }
        }
    }
    private void sendInactivityEmail(StationDto stationDto,LocalDateTime lastConnectionTime) throws EmailException {
        UserDto masterUser = customerAccountService.findMasterUserWithCustomerAccountId(stationDto.getCustomerAccountId());
        if (masterUser !=null) {
            String userName = masterUser.getUsername();
            Optional<UserRepresentation> recipientUser = keycloakService.getUserIdentity(userName);
            if (recipientUser.isPresent()) {
                // Send the email notification
                String userEmail = recipientUser.get().getEmail();
                String formattedLastUploadTime = lastConnectionTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                mailingService.sendInactivityEmail(userEmail, stationDto,formattedLastUploadTime);
                log.info("Inactivity email sent for station: " + stationDto.getControllerPts().getPtsId() + " to user: " + userEmail);
            }
        } else {
            log.warn("No associated user found for station: " + stationDto.getControllerPts().getPtsId());
        }
    }

}
