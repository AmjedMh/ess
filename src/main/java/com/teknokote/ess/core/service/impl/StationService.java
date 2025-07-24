package com.teknokote.ess.core.service.impl;

import com.teknokote.core.service.ActivatableGenericCheckedService;
import com.teknokote.ess.core.dao.CustomerAccountDao;
import com.teknokote.ess.core.dao.StationDao;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.model.requests.EnumRequestStatus;
import com.teknokote.ess.core.repository.FuelGradesRepository;
import com.teknokote.ess.core.service.impl.validators.StationValidator;
import com.teknokote.ess.core.service.mappers.FuelGradeConfigMapper;
import com.teknokote.ess.core.service.requests.FuelGradePriceChangeRequestService;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UploadedRecordsInformationDto;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.dto.requests.FuelGradePriceChangeRequestDto;
import com.teknokote.ess.events.publish.CustomerAccountExport;
import com.teknokote.ess.http.logger.EntityActionEvent;
import com.teknokote.ess.wsserveur.ESSWebSocketHandler;
import com.teknokote.pts.client.request.get.GetDateTimeRequestPacket;
import com.teknokote.pts.client.request.get.GetUploadedRecordsInformationRequestPacket;
import com.teknokote.pts.client.request.set.SetFuelGradesConfigurationRequestPacket;
import com.teknokote.pts.client.request.set.SetUsersConfigurationRequestPacket;
import com.teknokote.pts.client.response.configuration.PTSDateTime;
import com.teknokote.pts.client.response.configuration.PTSPermission;
import com.teknokote.pts.client.response.configuration.PTSUsers;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Getter
public class StationService extends ActivatableGenericCheckedService<Long, StationDto> {
    @Autowired
    private StationValidator validator;
    @Autowired
    private StationDao dao;
    @Autowired
    private CustomerAccountDao customerAccountDao;
    @Autowired
    private FuelGradeConfigMapper fuelGradeConfigMapper;
    @Autowired
    private ESSWebSocketHandler essWebSocketHandler;
    @Autowired
    private FuelGradesRepository fuelGradesRepository;
    @Autowired
    private FuelGradePriceChangeRequestService fuelGradePriceChangeRequestService;
    @Autowired
    private ControllerPtsConfigurationService controllerPtsConfigurationService;
    @Autowired
    private CustomerAccountExport customerAccountExport;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public int countStations(Long customerAccountId) {
        return getDao().countStations(customerAccountId);
    }

    /**
     * Modifies fuel grade prices on the controller of the station
     *
     * @param stationId
     * @param fuelGradeConfigDto
     */
    @Transactional
    public void changeFuelGradePrices(Long stationId, FuelGradeConfigDto fuelGradeConfigDto) {
        final StationDto stationDto = checkedFindById(stationId);
        List<FuelGradeConfigDto> fuelGradeConfigsToUpdate = findFuelGradeByStation(stationId).stream().filter(fuelGrade -> !fuelGrade.getId().equals(fuelGradeConfigDto.getId())).collect(Collectors.toList());
        fuelGradeConfigsToUpdate.add(fuelGradeConfigDto);
        SetFuelGradesConfigurationRequestPacket requestPacket = SetFuelGradesConfigurationRequestPacket.builder()
                .fuelGrades(fuelGradeConfigMapper.toPtsFuelGradeList(fuelGradeConfigsToUpdate))
                .id(1L)
                .build();
        essWebSocketHandler.sendRequests(stationDto.getControllerPts().getPtsId(), List.of(requestPacket));
    }

    @Transactional
    public PTSDateTime getControllerDateTime(Long stationId) {
        final StationDto stationDto = checkedFindById(stationId);
        GetDateTimeRequestPacket requestPacket = GetDateTimeRequestPacket.builder()
                .id(1L)
                .build();
        return essWebSocketHandler.getDateTimeFromController(stationDto.getControllerPts().getPtsId(), List.of(requestPacket));
    }

    public UploadedRecordsInformationDto getControllerUploadedInformation(String ptsId) {
        GetUploadedRecordsInformationRequestPacket requestPacket = GetUploadedRecordsInformationRequestPacket.builder()
                .id(1L)
                .build();
       return essWebSocketHandler.getUploadedInformationFromController(ptsId, List.of(requestPacket));
    }


    /**
     * Plans a fuel grade price change
     *
     * @param stationId
     * @param fuelGradeConfigDto
     */
    public void planFuelGradePriceChange(Long stationId, User connectedUser, FuelGradeConfigDto fuelGradeConfigDto, HttpServletRequest request) {
        StationDto stationDto=checkedFindById(stationId);
        FuelGradePriceChangeRequestDto fuelGradePriceChangeRequest = FuelGradePriceChangeRequestDto.builder()
                .plannedDate(fuelGradeConfigDto.getPlannedDate())
                .fuelGradeId(fuelGradeConfigDto.getId())
                .scheduledDate(fuelGradeConfigDto.getScheduledDate())
                .newPrice(fuelGradeConfigDto.getPrice())
                .stationId(stationId)
                .requesterId(connectedUser.getId())
                .status(EnumRequestStatus.PLANNED)
                .build();
        fuelGradePriceChangeRequestService.create(fuelGradePriceChangeRequest);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Planification mise à jour pour  carburant "+"'"+fuelGradeConfigDto.getName()+"'"+ " pour la  station " +"'"+ stationDto.getName()+"'" +"pour le  compte client "+"'"+stationDto.getCustomerAccountName()+"'",
                connectedUser,
                request
        );
        eventPublisher.publishEvent(event);
    }

    public void cancelFuelGradePriceChangePlanification(Long stationId,Long fuelGradePriceChangeId,HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StationDto stationDto =checkedFindById(stationId);
        FuelGradePriceChangeRequestDto fuelGradePriceChangeRequest = fuelGradePriceChangeRequestService.checkedFindById(fuelGradePriceChangeId);
        fuelGradePriceChangeRequest.setStatus(EnumRequestStatus.CANCELED);
        fuelGradePriceChangeRequestService.update(fuelGradePriceChangeRequest);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Annulation  de mise à jour du carburant "+"'"+fuelGradePriceChangeRequest.getFuelGrade().getName()+"'"+ " pour la station station " +"'"+ stationDto.getName()+"'" +"pour le  compte client "+"'"+stationDto.getCustomerAccountName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);

    }

    public List<FuelGradeConfigDto> findFuelGradeByStation(Long stationId) {
        final StationDto stationDto = checkedFindById(stationId);
        final ControllerPtsConfiguration currentConfigurationOnController = controllerPtsConfigurationService.findCurrentConfigurationOnController(stationDto.getControllerPts().getId());
        return fuelGradeConfigMapper.toDtoFuelGradeList(fuelGradesRepository.findFuelGradesByControllerConfiguration(currentConfigurationOnController));
    }

    public List<FuelGradePriceChangeRequestDto> findPlannedFuelGradePriceChangeByStation(
            Long stationId, String fuelName, String requesterName, String price,
            LocalDateTime startDate, LocalDateTime endDate) {

        checkedFindById(stationId);
        List<FuelGradePriceChangeRequestDto> fuelGradePriceChangeRequest = fuelGradePriceChangeRequestService.findPlannedByStation(stationId)
                .stream()
                .filter(dto -> !dto.getStatus().equals(EnumRequestStatus.EXECUTED))
                .toList();

        fuelGradePriceChangeRequest = fuelGradePriceChangeRequest.stream()
                .filter(dto -> (fuelName == null || fuelName.isEmpty() || dto.getFuelGrade().getName().equalsIgnoreCase(fuelName))
                        && (requesterName == null || requesterName.isEmpty() || dto.getRequester().getUsername().equalsIgnoreCase(requesterName))
                        && (price == null || price.isEmpty() || (dto.getNewPrice() != null && dto.getNewPrice().compareTo(Double.valueOf(price)) == 0))
                        && (startDate == null || (dto.getPlannedDate() != null && dto.getPlannedDate().isAfter(startDate)))
                        && (endDate == null || (dto.getPlannedDate() != null && dto.getPlannedDate().isBefore(endDate))))
                .sorted(Comparator.comparing(FuelGradePriceChangeRequestDto::getPlannedDate).reversed())
                .toList();

        return fuelGradePriceChangeRequest;
    }

    public List<FuelGradePriceChangeRequestDto> findExecutedFuelGradePriceChangeByStation(
            Long stationId, String fuelName, String requesterName, String price,
            LocalDateTime startDate, LocalDateTime endDate) {
        List<FuelGradePriceChangeRequestDto> executedChanges =
                fuelGradePriceChangeRequestService.findExecutedByStation(stationId);

        // Apply filters
        executedChanges = executedChanges.stream()
                .filter(dto -> (fuelName == null || fuelName.isEmpty() || dto.getFuelGrade().getName().equalsIgnoreCase(fuelName))
                        && (requesterName == null || requesterName.isEmpty() || dto.getRequester().getUsername().equalsIgnoreCase(requesterName))
                        && (price == null || price.isEmpty() || (dto.getNewPrice() != null && dto.getNewPrice().compareTo(Double.valueOf(price)) == 0))
                        && (startDate == null || (dto.getPlannedDate() != null && dto.getPlannedDate().isAfter(startDate)))
                        && (endDate == null || (dto.getPlannedDate() != null && dto.getPlannedDate().isBefore(endDate)))
                )
                .sorted(Comparator.comparing(FuelGradePriceChangeRequestDto::getPlannedDate).reversed())
                .toList();

        return executedChanges;
    }

    public FuelGradeConfigDto getFuelGradeByStation(Long stationId, Long fuelId) {
        final StationDto stationDto = checkedFindById(stationId);
        final ControllerPtsConfiguration currentConfigurationOnController = controllerPtsConfigurationService.findCurrentConfigurationOnController(stationDto.getControllerPts().getId());
        FuelGrade fuel = fuelGradesRepository.findFuelGradesByControllerConfigurationAndId(currentConfigurationOnController, fuelId);
        FuelGradeConfigDto gradeConfigDto = fuelGradeConfigMapper.toDtoFuelGrade(fuel);
        return gradeConfigDto;
    }

    public FuelGradeConfigDto getFuelGradeByStationAndConfiguration(Long stationId, Long idConf) {
        final StationDto stationDto = checkedFindById(stationId);
        final ControllerPtsConfiguration currentConfigurationOnController = controllerPtsConfigurationService.findCurrentConfigurationOnController(stationDto.getControllerPts().getId());
        return fuelGradeConfigMapper.toDtoFuelGrade(fuelGradesRepository.findFuelGradesByControllerConfigurationAndIdConf(currentConfigurationOnController, idConf));
    }

    public void changeUserController(Long stationId, UserDto userDto) {
        final StationDto stationDto = checkedFindById(stationId);
        PTSPermission ptsPermission = PTSPermission.builder().control(true).reports(true).monitoring(true).configuration(true).build();
        PTSUsers ptsUsers = PTSUsers.builder().id(1L).login(userDto.getUsername()).password(userDto.getPassword()).permission(ptsPermission).build();
        SetUsersConfigurationRequestPacket requestPacket = SetUsersConfigurationRequestPacket.builder()
                .users(Collections.singletonList(ptsUsers))
                .id(1L)
                .build();
        essWebSocketHandler.sendRequests(stationDto.getControllerPts().getPtsId(), List.of(requestPacket));
    }
}
