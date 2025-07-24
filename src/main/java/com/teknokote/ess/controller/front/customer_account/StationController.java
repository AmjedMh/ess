package com.teknokote.ess.controller.front.customer_account;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.impl.CustomerAccountService;
import com.teknokote.ess.core.service.impl.StationService;
import com.teknokote.ess.core.service.requests.FuelGradePriceChangeRequestService;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UploadedRecordsInformationDto;
import com.teknokote.ess.dto.requests.FuelGradePriceChangeRequestDto;
import com.teknokote.pts.client.response.configuration.PTSDateTime;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@CrossOrigin("*")
@RequestMapping(EndPoints.CUSTOMER_ACCOUNT_STATION_ROOT)
public class StationController {
    @Autowired
    private StationService stationService;
    @Autowired
    private CustomerAccountService customerAccountService;
    @Autowired
    private FuelGradePriceChangeRequestService fuelGradePriceChangeRequestService;

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping()
    public List<StationDto> getAllStationForUser(@PathVariable Long customerAccountId) {
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customerAccountService.findStationsByCustomerAccount(connectedUser);
    }

    /**
     * customerAccountId est utilisé seulement pour le contrôle de droit
     */
    @PreAuthorize("isUserAttachedToCustomerAccount(#customerAccountId)")
    @PostMapping(EndPoints.ADD)
    public ResponseEntity<Object> addStationToCustomerAccount(@PathVariable Long customerAccountId, @RequestBody StationDto dto,HttpServletRequest servletRequest) {
        try {
            StationDto savedStation = customerAccountService.addStation(dto,servletRequest);
            return new ResponseEntity<>(savedStation, HttpStatus.CREATED);
        } catch (ServiceValidationException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping(EndPoints.LIST_OF_STATION)
    public Page<StationDto> listStationByCustomerAccount(@PathVariable Long customerAccountId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<StationDto> allStations = customerAccountService.findStationsByCustomerAccount(connectedUser);
        allStations.sort(Comparator.comparing(StationDto::getCreatedDate));
        int start = page * size;
        int end = Math.min((start + size), allStations.size());
        return new PageImpl<>(allStations.subList(start, end), PageRequest.of(page, size), allStations.size());
    }

    @PreAuthorize("isUserAttachedToCustomerAccount(#customerAccountId)")
    @PutMapping(EndPoints.DEACTIVATE)
    public ResponseEntity<StationDto> deactivateStation(@PathVariable Long customerAccountId, @PathVariable Long id) {
        return ResponseEntity.ok(stationService.deactivate(id));
    }

    @PreAuthorize("isUserAttachedToCustomerAccount(#customerAccountId)")
    @PutMapping(EndPoints.ACTIVATE)
    public ResponseEntity<StationDto> activateStation(@PathVariable Long customerAccountId, @PathVariable Long id) {
        return ResponseEntity.ok(stationService.activate(id));
    }

    @PreAuthorize("isUserAttachedToCustomerAccount(#customerAccountId)")
    @PutMapping(EndPoints.UPDATE)
    public ResponseEntity<StationDto> updateStation(@PathVariable Long customerAccountId, @RequestBody StationDto stationDto, HttpServletRequest servletRequest) {
        final StationDto updatedStation = customerAccountService.updateStation(stationDto,servletRequest);
        return ResponseEntity.ok(updatedStation);
    }

    @PreAuthorize("isUserAttachedToCustomerAccount(#customerAccountId)")
    @GetMapping(EndPoints.STATION_INFO)
    public Optional<StationDto> stationDetails(@PathVariable Long customerAccountId, @PathVariable Long id) {
        return stationService.findById(id);
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping(EndPoints.LIST_BY_FILTER)
    public Page<StationDto> listStationByFilter(@PathVariable Long customerAccountId,
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) String creator,
                                                @RequestParam(required = false) String parent,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size) {
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (size != 25 && size != 50 && size != 100 && size != 200 && size != 500) {
            throw new IllegalArgumentException("Invalid page size. Allowed values are 25, 50, 100, 500.");
        }
        List<StationDto> filteredStations = customerAccountService.findStationByFilter(connectedUser, name, creator, parent);
        int start = page * size;
        int end = Math.min((start + size), filteredStations.size());
        return new PageImpl<>(filteredStations.subList(start, end), PageRequest.of(page, size), filteredStations.size());
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping(EndPoints.SEARCH)
    public List<StationDto> findStation(@RequestParam(required = false) String name, @PathVariable Long customerAccountId) {
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<StationDto> allStations = customerAccountService.findStationsByCustomerAccount(connectedUser);
        return allStations.stream().filter(stationDto -> StringUtils.containsIgnoreCase(stationDto.getName(), name)).toList();
    }


    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @PostMapping(EndPoints.FUEL_GRADE_PRICE)
    public void sendCmdToController(@PathVariable Long customerAccountId, @PathVariable Long stationId, @RequestBody FuelGradeConfigDto fuelGrade) {
        stationService.changeFuelGradePrices(stationId, fuelGrade);
    }

    @PostMapping(EndPoints.GET_DATE_TIME)
    public PTSDateTime getControllerDateTime(@PathVariable Long customerAccountId, @PathVariable Long stationId) {
        return stationService.getControllerDateTime(stationId);
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @PostMapping(EndPoints.PLAN_FUEL_GRADE_PRICE_CHANGE)
    public void planPriceModification(@PathVariable Long customerAccountId, @PathVariable Long stationId, @RequestBody FuelGradeConfigDto fuelGrade,HttpServletRequest request) {
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        stationService.planFuelGradePriceChange(stationId, connectedUser, fuelGrade,request);
    }

    @PreAuthorize("isUserAttachedToCustomerAccount(#customerAccountId)")
    @PostMapping(EndPoints.CANCEL_PLANNED_FUEL_GRADE_PRICE_CHANGE)
    public void cancelFuelGradePriceChange(@PathVariable Long customerAccountId,@PathVariable Long stationId, @PathVariable Long id,HttpServletRequest request) {
        stationService.cancelFuelGradePriceChangePlanification(stationId,id,request);
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @PostMapping(EndPoints.UPDATE_PLANNED_FUEL_GRADE_PRICE_CHANGE)
    public void updateFuelGradePriceChange(@PathVariable Long customerAccountId,
                                           @PathVariable Long stationId,
                                           @RequestBody FuelGradePriceChangeRequestDto fuelGradeRequest,HttpServletRequest request) {
        fuelGradePriceChangeRequestService.updatePlannedFuelGradeRequest(stationId,fuelGradeRequest,request);
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping(EndPoints.FUEL_GRADE_INFO)
    public FuelGradeConfigDto getFuelGradeByStation(@PathVariable Long customerAccountId, @PathVariable Long stationId, @PathVariable Long id) {

        return stationService.getFuelGradeByStation(stationId, id);
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping(EndPoints.FUEL_GRADE_LIST)
    public List<FuelGradeConfigDto> findFuelGradesByStation(@PathVariable Long customerAccountId, @PathVariable Long stationId) {

        return stationService.findFuelGradeByStation(stationId);
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping(EndPoints.FUEL_GRADE_PRICE_CHANGE_REQUEST_INFO)
    public FuelGradePriceChangeRequestDto getGradePriceChangeRequest(@PathVariable Long customerAccountId, @PathVariable Long stationId, @PathVariable Long id) {
        return fuelGradePriceChangeRequestService.findFuelGradePriceChangeRequestByStation(stationId, id);
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping(EndPoints.PLANNED_FUEL_GRADE_PRICE_CHANGE_LIST)
    public List<FuelGradePriceChangeRequestDto> findPlannedFuelGradePriceChangeByStation(@PathVariable Long customerAccountId,
                                                                                         @PathVariable Long stationId,
                                                                                         @RequestParam(required = false) String fuelName,
                                                                                         @RequestParam(required = false) String requesterName,
                                                                                         @RequestParam(required = false) String price,
                                                                                         @RequestParam(required = false) LocalDateTime startDate,
                                                                                         @RequestParam(required = false) LocalDateTime endDate) {

        return stationService.findPlannedFuelGradePriceChangeByStation(stationId, fuelName, requesterName, price, startDate, endDate);
    }

    @PreAuthorize("isUserAttachedToParentOrCustomerAccountOf(#customerAccountId)")
    @GetMapping(EndPoints.EXECUTED_FUEL_GRADE_PRICE_CHANGE_LIST)
    public List<FuelGradePriceChangeRequestDto> findExecutedFuelGradePriceChangeByStation(@PathVariable Long customerAccountId,
                                                                                          @PathVariable Long stationId,
                                                                                          @RequestParam(required = false) String fuelName,
                                                                                          @RequestParam(required = false) String requesterName,
                                                                                          @RequestParam(required = false) String price,
                                                                                          @RequestParam(required = false) LocalDateTime startDate,
                                                                                          @RequestParam(required = false) LocalDateTime endDate) {

        return stationService.findExecutedFuelGradePriceChangeByStation(stationId, fuelName, requesterName, price, startDate, endDate);
    }


    @PostMapping(EndPoints.GET_UPLOADED_INFORMATION)
    public UploadedRecordsInformationDto getControllerUploadedInformation(@PathVariable String ptsId) {
        return stationService.getControllerUploadedInformation(ptsId);
    }
}
