package com.teknokote.ess.core.service.impl.shifts;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.dao.shifts.PumpAttendantCollectionSheetDao;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningExecutionDao;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningExecutionDetailDao;
import com.teknokote.ess.core.dao.shifts.WorkDayShiftPlanningExecutionDao;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Nozzle;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.core.model.organization.EnumAffectationMode;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.model.shifts.ShiftExecutionStatus;
import com.teknokote.ess.core.service.PumpAttendantService;
import com.teknokote.ess.core.service.impl.ControllerPtsConfigurationService;
import com.teknokote.ess.core.service.impl.PumpService;
import com.teknokote.ess.core.service.impl.StationService;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.core.service.impl.validators.shifts.ShiftDetailUpdatesValidator;
import com.teknokote.ess.core.service.mappers.ShiftPlanningExecutionDtoMapper;
import com.teknokote.ess.core.service.shifts.*;
import com.teknokote.ess.dto.ControllerPtsDto;
import com.teknokote.ess.dto.PumpAttendantDto;
import com.teknokote.ess.dto.ShiftDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningExecutionDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import com.teknokote.ess.dto.shifts.*;
import com.teknokote.ess.exceptions.ShiftExecutionException;
import com.teknokote.ess.http.logger.EntityActionEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
public class ShiftPlanningExecutionServiceImpl
        extends GenericCheckedService<Long, ShiftPlanningExecutionDto> implements ShiftPlanningExecutionService {
    @Autowired
    private ESSValidator<ShiftPlanningExecutionDto> validator;
    @Autowired
    private ShiftPlanningExecutionDao dao;
    @Autowired
    private PumpAttendantCollectionSheetService pumpAttendantCollectionSheetService;
    @Autowired
    private PumpAttendantCollectionSheetDao pumpAttendantCollectionSheetDao;
    @Autowired
    private ShiftPlanningExecutionDetailDao detailDao;
    @Autowired
    private ShiftPlanningService shiftPlanningService;
    @Autowired
    private PumpService pumpService;
    @Autowired
    private StationService stationService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ShiftPlanningExecutionDtoMapper shiftPlanningExecutionDtoMapper;
    @Autowired
    private PumpAttendantService pumpAttendantService;
    @Autowired
    private ShiftDetailUpdatesValidator shiftDetailUpdatesValidator;
    @Autowired
    private PaymentMethodService paymentMethodService;
    @Autowired
    private WorkDayShiftPlanningService workDayShiftPlanningService;
    @Autowired
    private WorkDayShiftPlanningExecutionService workDayShiftPlanningExecutionService;
    @Autowired
    private WorkDayShiftPlanningExecutionDao workDayShiftPlanningExecutionDao;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private ControllerPtsConfigurationService controllerPtsConfigurationService;
    private static final String FOR_STATION = " pour la  station ";
    public static final String SHIFT_STATUS= "Shift planning execution is at status :[";

    @Override
    @Transactional
    public List<DynamicShiftPlanningExecutionDto> generate(LocalDate day, Long stationId) {
        WorkDayShiftPlanningDto workDayShiftPlanning = workDayShiftPlanningService.findByStationAndDay(stationId, day);
        List<ShiftPlanningExecutionDto> existingExecutions = null;
        if (workDayShiftPlanning != null) {
            existingExecutions = findExistingShiftPlanningExecutions(stationId, workDayShiftPlanning.getId());
        }
        return CollectionUtils.isNotEmpty(existingExecutions) ? mapExistingExecutions(existingExecutions) : mapNewExecutions(workDayShiftPlanning);
    }

    public List<DynamicShiftPlanningExecutionDto> mapNewExecutions(WorkDayShiftPlanningDto workDayShiftPlanning) {
        if (workDayShiftPlanning == null) {
            throw new ServiceValidationException("No Scheduled generated !! please generate scheduled for current rotation to access the agenda");
        }
        Set<ShiftPlanningDto> shiftPlanningDtos = workDayShiftPlanning.getShiftPlannings();
        if (shiftPlanningDtos.stream().anyMatch(el -> !isOffDay(el.getShift()) && Objects.isNull(el.getPumpAttendantTeamId()))) {
            throw new ServiceValidationException("One or more shifts are unaffected to teams!");
        }
        WorkDayShiftPlanningExecutionDto workDayShiftPlanningExecution = workDayShiftPlanningExecutionDao.create(WorkDayShiftPlanningExecutionDto.builder()
                .workDayShiftPlanningId(workDayShiftPlanning.getId()).build());
        AtomicInteger index = new AtomicInteger(1);
        return shiftPlanningDtos.stream()
                .filter(shiftPlanningDto -> !isOffDay(shiftPlanningDto.getShift()))
                .map(shiftPlanningDto -> generateForShiftPlanning(workDayShiftPlanningExecution, shiftPlanningDto, index.getAndIncrement())) // Pass index and increment it
                .map(this::create)
                .map(shiftPlanningExecutionDtoMapper::toDynamic)
                .sorted(Comparator.comparing(DynamicShiftPlanningExecutionDto::getShiftIndex))
                .collect(Collectors.toList());
    }

    public boolean isOffDay(ShiftDto shiftDto) {
        return shiftDto != null && Boolean.TRUE.equals(shiftDto.getOffDay());
    }

    public List<DynamicShiftPlanningExecutionDto> mapExistingExecutions(List<ShiftPlanningExecutionDto> existingExecutions) {
        return existingExecutions.stream().map(shiftPlanningExecutionDtoMapper::toDynamic).sorted(Comparator.comparing(DynamicShiftPlanningExecutionDto::getShiftIndex)).collect(Collectors.toList());
    }

    public ShiftPlanningExecutionDto stop(Long stationId, Long shiftPlanningExecutionId, LocalDateTime endDateTime, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WorkDayShiftPlanningDto workDayShiftPlanning = workDayShiftPlanningService.findByStationAndDay(stationId, endDateTime.toLocalDate());
        Optional<ShiftPlanningExecutionDto> shiftPlanningExecution = getDao().findByStation(shiftPlanningExecutionId, stationId);
        ShiftPlanningExecutionDto shiftExecution = shiftPlanningExecution.orElseThrow(() -> new ShiftExecutionException("Shift planning execution not found !!."));
        if (shiftExecution.isNotInProgress()) {
            throw new ServiceValidationException(SHIFT_STATUS + shiftExecution.getStatus() + "] and can't be STOPPED !!! ");
        }
        if (paymentMethodService.findByStationId(stationId).isEmpty()) {
            throw new ServiceValidationException("Payment methods not found , Please add one at least to lock this shift !!! ");
        }
        LocalDateTime realEndDateTime = Objects.isNull(endDateTime) ? LocalDateTime.now() : endDateTime;
        if (realEndDateTime.isBefore(shiftExecution.getStartDateTime())) {
            throw new ServiceValidationException("L'heure de début doit être inférieure ou égale à l'heure de fin. Veuillez vérifier les horaires.");
        }
        if (realEndDateTime.isBefore(shiftExecution.getEndDateTime())) {
            List<ShiftPlanningExecutionDto> shiftExecutions = findExistingShiftPlanningExecutions(stationId, workDayShiftPlanning.getId());
            int currentShiftExecutionIndex = shiftExecution.getIndex();
            if (currentShiftExecutionIndex < shiftExecutions.size()) {
                ShiftPlanningExecutionDto nextShiftExecution = shiftExecutions.stream().filter(shiftPlanningExecutionDto -> shiftPlanningExecutionDto.getIndex() == currentShiftExecutionIndex + 1).toList().get(0);
                nextShiftExecution.setStartDateTime(realEndDateTime);
                for (ShiftPlanningExecutionDetailDto nextShiftDetail : nextShiftExecution.getShiftPlanningExecutionDetail()) {
                    if (!nextShiftDetail.isForced()) {
                        nextShiftDetail.setStartDateTime(realEndDateTime);
                    }
                }
                getDao().update(nextShiftExecution);
            }
        }
        shiftExecution.stopExecution(realEndDateTime);
        final StationDto stationDto = stationService.checkedFindById(stationId);
        Map<Long, BigDecimal> pumpAttendantSheetMap = new HashMap<>();
        // stop execution details
        for (ShiftPlanningExecutionDetailDto shiftDetail : shiftExecution.getShiftPlanningExecutionDetail()) {
            Optional<PumpTransaction> lastTransactionOnDate;
            if (EnumAffectationMode.MANUEL.equals(stationDto.getModeAffectation())) {
                lastTransactionOnDate = transactionService.findLastTransactionOnDate(stationDto.getControllerPts().getPtsId(), shiftDetail.getNozzle().getIdConf(), shiftDetail.getPump().getIdConf(), realEndDateTime);
            } else {
                lastTransactionOnDate = transactionService.findLastTransactionOnDateByTag(stationDto.getControllerPts().getPtsId(), shiftDetail.getNozzle().getIdConf(), shiftDetail.getPump().getIdConf(), shiftDetail.getPumpAttendant().getTag(), realEndDateTime);
            }
            if (shiftDetail.isForced()) {
                final Optional<PumpTransaction> firstTransactionOnDate = transactionService.findFirstTransactionOnDate(stationDto.getControllerPts().getPtsId(), shiftDetail.getNozzle().getIdConf(), shiftDetail.getPump().getIdConf(), shiftDetail.getStartDateTime());
                BigDecimal startIndexVolume = firstTransactionOnDate.map(PumpTransaction::getTotalVolume).orElse(BigDecimal.ZERO);
                BigDecimal startIndexAmount = firstTransactionOnDate.map(PumpTransaction::getTotalAmount).orElse(BigDecimal.ZERO);
                shiftDetail.setStartIndex(startIndexVolume);
                shiftDetail.setStartIndexAmount(startIndexAmount);
            }
            BigDecimal endIndexVolume = lastTransactionOnDate.map(PumpTransaction::getTotalVolume).orElse(BigDecimal.ZERO);
            BigDecimal endIndexAmount = lastTransactionOnDate.map(PumpTransaction::getTotalAmount).orElse(BigDecimal.ZERO);
            shiftDetail.stopExecution(realEndDateTime, endIndexVolume, endIndexAmount);
            // Aggregatem totalAmount for each pumpAttendant
            Long pumpAttendantId = shiftDetail.getPumpAttendantId();
            BigDecimal totalAmount = pumpAttendantSheetMap.getOrDefault(pumpAttendantId, BigDecimal.ZERO).add(shiftDetail.getTotalAmount());
            pumpAttendantSheetMap.put(pumpAttendantId, totalAmount);
        }
        // Iterate over pumpAttendants and initiate PumpAttendantCollectionSheet
        shiftExecution.createPumpAttendantSheets(pumpAttendantSheetMap);
        ShiftPlanningExecutionDto planningExecutionDto = getDao().update(shiftExecution);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Arret  shift " + "'" + shiftExecution.getShift().getName() + "'" + FOR_STATION + "'" + stationDto.getName() + "'" + " pour le compte client " + "'" + stationDto.getCustomerAccountName() + "'",
                (User) authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return planningExecutionDto;
    }

    @Override
    @Transactional
    public ShiftPlanningExecutionDto start(Long stationId, Long shiftPlanningExecutionId, LocalDateTime startDateTime, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<ShiftPlanningExecutionDto> shiftPlanningExecution = getDao().findByStation(shiftPlanningExecutionId, stationId);
        ShiftPlanningExecutionDto shiftExecution = shiftPlanningExecution.orElseThrow(() -> new ShiftExecutionException("Shift planning execution not found !!."));
        if (shiftExecution.isNotWaiting()) {
            throw new ServiceValidationException(SHIFT_STATUS + shiftExecution.getStatus() + "] and can't be started !!! ");
        }
        if (shiftExecution.getStartDateTime().toLocalDate().isAfter(LocalDate.now())) {
            throw new ServiceValidationException("Shift planning execution cannot be started before its scheduled start date.");
        }
        ShiftPlanningExecutionDto inProgressExecution = getDao().findInProgressExecution(stationId);
        if (inProgressExecution != null) {
            throw new ServiceValidationException("Impossible de démarrer un autre poste tant que le poste " + inProgressExecution.getShift().getName() + " est en cours");
        }
        final LocalDateTime realStartDateTime = Objects.isNull(startDateTime) ? LocalDateTime.now() : startDateTime;
        List<ShiftPlanningExecutionDto> previousExecutions = getDao().findByStationAndDay(stationId, LocalDate.from(realStartDateTime));

        if (!previousExecutions.isEmpty()) {
            Optional<ShiftPlanningExecutionDto> previousExecution = previousExecutions.stream()
                    .filter(dto -> dto.getStartDateTime().isBefore(shiftExecution.getStartDateTime()))
                    .findFirst();

            if (previousExecution.isPresent() && previousExecution.get().getStatus().equals(ShiftExecutionStatus.WAITING)) {
                throw new ServiceValidationException("Shift planning execution cannot be started, the previous shift has not started yet!!");
            }
        }
        shiftExecution.start(realStartDateTime);
        // Start execution details:affect only not forced execution details
        final StationDto station = stationService.checkedFindById(stationId);
        BigDecimal startIndexVolume;
        BigDecimal startIndexAmount;
        for (ShiftPlanningExecutionDetailDto shiftDetail : shiftExecution.getShiftPlanningExecutionDetail()) {
            final Optional<PumpTransaction> firstTransactionOnDate;
            if (EnumAffectationMode.MANUEL.equals(station.getModeAffectation())) {
                firstTransactionOnDate = transactionService.findFirstTransactionOnDate(station.getControllerPts().getPtsId(), shiftDetail.getNozzle().getIdConf(), shiftDetail.getPump().getIdConf(), realStartDateTime);
            } else {
                firstTransactionOnDate = transactionService.findFirstTransactionOnDateByTag(station.getControllerPts().getPtsId(), shiftDetail.getNozzle().getIdConf(), shiftDetail.getPump().getIdConf(), shiftDetail.getPumpAttendant().getTag(), realStartDateTime);
            }
            startIndexVolume = firstTransactionOnDate.map(PumpTransaction::getTotalVolume).orElse(BigDecimal.ZERO);
            startIndexAmount = firstTransactionOnDate.map(PumpTransaction::getTotalAmount).orElse(BigDecimal.ZERO);
            if (!shiftDetail.isForced() || shiftExecution.getStartDateTime().isAfter(shiftDetail.getStartDateTime())) {
                shiftDetail.startExecution(realStartDateTime, startIndexVolume, startIndexAmount);
            }
        }
        update(shiftExecution);
        shiftPlanningService.markAsExecuting(shiftExecution.getShiftPlanningId());
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Démarrage  shift " + "'" + shiftExecution.getShift().getName() + "'" + FOR_STATION + "'" + station.getName() + "'" + " pour le compte client " + "'" + station.getCustomerAccountName() + "'",
                (User) authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return shiftExecution;

    }

    /**
     * Rechercher les exécutions existantes des plannings
     */
    @Override
    public List<ShiftPlanningExecutionDto> findExistingShiftPlanningExecutions(Long stationId, Long workDayId) {
        return getDao().findByWorkDayAndStation(stationId, workDayId);
    }

    ShiftPlanningExecutionDto generateForShiftPlanning(WorkDayShiftPlanningExecutionDto workDayShiftPlanningExecution, ShiftPlanningDto shiftPlanningDto, int index) {
        PumpAttendantTeamDto pumpAttendantTeamDto = shiftPlanningDto.getPumpAttendantTeam();
        WorkDayShiftPlanningDto workDayShiftPlanning = workDayShiftPlanningService.checkedFindById(shiftPlanningDto.getWorkDayShiftPlanningId());
        ShiftDto shiftDto = shiftPlanningDto.getShift();
        LocalDateTime endDateTime = LocalDateTime.of(workDayShiftPlanning.getDay(), shiftDto.getEndingTime());
        if (shiftDto.isCrossesDayBoundary()) {
            endDateTime = endDateTime.plusDays(1);
        }
        // Générer les détails
        final List<ShiftPlanningExecutionDetailDto> shiftPlanningExecutionDetailDtos = pumpAttendantTeamDto.getAffectedPumpAttendant()
                .stream()
                .flatMap(el -> this.generateExecutionDetailsPerPumpAttendant(el, workDayShiftPlanning, shiftPlanningDto).stream())
                .collect(Collectors.toList());
        shiftPlanningExecutionDetailDtos.forEach(el -> el.setIndex(index));
        return ShiftPlanningExecutionDto.builder()
                .index(index)
                .shiftPlanningId(shiftPlanningDto.getId())
                .status(ShiftExecutionStatus.WAITING)
                .workDayShiftPlanningExecutionId(workDayShiftPlanningExecution.getId())
                .startDateTime(LocalDateTime.of(workDayShiftPlanning.getDay(), shiftPlanningDto.getShift().getStartingTime()))
                .endDateTime(endDateTime)
                .shiftId(shiftPlanningDto.getShiftId())
                .shiftPlanningExecutionDetail(shiftPlanningExecutionDetailDtos)
                .build();
    }

    /**
     * Generates a list of ShiftPlanningExecutionDetailDto objects per pump attendant.
     *
     * @param affectedPumpAttendantDto The affected pump attendant.
     * @return List of ShiftPlanningExecutionDetailDto objects.
     */
    public List<ShiftPlanningExecutionDetailDto> generateExecutionDetailsPerPumpAttendant(AffectedPumpAttendantDto affectedPumpAttendantDto, WorkDayShiftPlanningDto workDayShiftPlanning, ShiftPlanningDto shiftPlanningDto) {
        StationDto stationDto = stationService.checkedFindById(shiftPlanningDto.getStationId());
        ControllerPtsDto controllerPtsDto = stationDto.getControllerPts();
        ControllerPtsConfiguration controllerPtsConfiguration = controllerPtsConfigurationService.findCurrentConfigurationOnController(controllerPtsDto.getId());
        ShiftDto shiftDto = shiftPlanningDto.getShift();
        LocalDateTime startDateTime = LocalDateTime.of(workDayShiftPlanning.getDay(), shiftPlanningDto.getShift().getStartingTime());
        LocalDateTime endDateTime = LocalDateTime.of(workDayShiftPlanning.getDay(), shiftPlanningDto.getShift().getEndingTime());

        if (shiftDto.isCrossesDayBoundary()) {
            endDateTime = endDateTime.plusDays(1);
        }
        final Optional<Pump> pump = pumpService.findByIdConfAndControllerPtsConfiguration(affectedPumpAttendantDto.getPumpId(), controllerPtsConfiguration);
        final List<Nozzle> nozzleList = pump.map(Pump::getNozzleList).orElse(List.of());
        LocalDateTime finalEndDateTime = endDateTime;
        return nozzleList.stream().map(nozzle -> ShiftPlanningExecutionDetailDto.builder()
                .pumpAttendantId(affectedPumpAttendantDto.getPumpAttendantId())
                .pumpId(affectedPumpAttendantDto.getPumpId())
                .tankReturn(BigDecimal.ZERO)
                .startDateTime(startDateTime)
                .endDateTime(finalEndDateTime)
                .startIndexAmount(BigDecimal.ZERO)
                .endIndexAmount(BigDecimal.ZERO)
                .startIndex(BigDecimal.ZERO)
                .endIndex(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .totalVolume(BigDecimal.ZERO)
                .tankReturn(BigDecimal.ZERO)
                .nozzle(nozzle)
                .nozzleId(nozzle.getId())
                .forced(false)
                .build()).collect(Collectors.toList());
    }

    public PumpAttendantCollectionSheetDto updateCollectionSheetForPumpAttendant(PumpAttendantCollectionSheetDto pumpAttendantCollectionSheetDto, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (checkedFindById(pumpAttendantCollectionSheetDto.getShiftPlanningExecutionId()).isCompleted()) {
            PumpAttendantDto pumpAttendantDto = pumpAttendantService.checkedFindById(pumpAttendantCollectionSheetDto.getPumpAttendantId());
            EntityActionEvent event = new EntityActionEvent(
                    this,
                    "Validation de recette du pompiste " + "'" + pumpAttendantDto.getFirstName() + "'",
                    (User) authentication.getPrincipal(),
                    request
            );
            eventPublisher.publishEvent(event);
            return pumpAttendantCollectionSheetService.update(pumpAttendantCollectionSheetDto);
        } else {
            throw new ShiftExecutionException("shiftPlanningExecution is InProgress or is not started");
        }
    }

    public BigDecimal calculateTotalAmountPumpAttendant(Long shiftPlanningExecutionId, Long pumpAttendantId) {
        List<ShiftPlanningExecutionDetailDto> detailsForPumpAttendant = detailDao.findByShiftPlanningExecutionIdAndPumpAttendantId(shiftPlanningExecutionId, pumpAttendantId);
        return detailsForPumpAttendant.stream()
                .map(ShiftPlanningExecutionDetailDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public ShiftPlanningExecutionDto lock(Long stationId, Long shiftPlanningExecutionId, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StationDto station = stationService.checkedFindById(stationId);
        final ShiftPlanningExecutionDto shiftPlanningExecutionDto = checkedFindById(shiftPlanningExecutionId);
        if (shiftPlanningExecutionDto.isNotCompleted()) {
            throw new ServiceValidationException(SHIFT_STATUS + shiftPlanningExecutionDto.getStatus() + "] and can't be LOCKED !!! ");
        }
        if (this.allSheetsTreated(shiftPlanningExecutionDto)) {
            shiftPlanningExecutionDto.markLocked();
            EntityActionEvent event = new EntityActionEvent(
                    this,
                    "Verouilaage du shift " + "'" + shiftPlanningExecutionDto.getShift().getName() + "'" + FOR_STATION + "'" + station.getName() + "'" + " pour le compte client " + "'" + station.getCustomerAccountName() + "'",
                    (User) authentication.getPrincipal(),
                    request
            );
            eventPublisher.publishEvent(event);
            return update(shiftPlanningExecutionDto);
        }
        throw new ServiceValidationException("Collection for one or more pumpAttendant are not created, please verify your shift recipe  ");
    }

    /**
     * Checks if all sheets are treated for a given ShiftPlanningExecutionDto.
     * Treated means FORCED or SOLDED
     *
     * @param shiftPlanningExecutionDto The ShiftPlanningExecutionDto object.
     * @return True if all sheets are treated, false otherwise.
     */
    public boolean allSheetsTreated(ShiftPlanningExecutionDto shiftPlanningExecutionDto) {
        List<PumpAttendantCollectionSheetDto> sheets = pumpAttendantCollectionSheetDao.findByShiftPlanningExecutionId(shiftPlanningExecutionDto.getId());
        // Get the ids of pumpAttendants from shiftPlanningExecutionDetail
        Set<Long> distinctPumpAttendantIds = shiftPlanningExecutionDto.getShiftPlanningExecutionDetail()
                .stream()
                .map(ShiftPlanningExecutionDetailDto::getPumpAttendantId)
                .collect(Collectors.toSet());
        // Check if the number of sheets matches the number of pumpAttendants
        return sheets.size() == distinctPumpAttendantIds.size() && sheets.stream().allMatch(PumpAttendantCollectionSheetDto::isTreated);

    }

    @Override
    public ShiftPlanningExecutionDto unlock(Long stationId, Long shiftPlanningExecutionId, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StationDto station = stationService.checkedFindById(stationId);
        final ShiftPlanningExecutionDto shiftPlanningExecutionDto = checkedFindById(shiftPlanningExecutionId);
        if (shiftPlanningExecutionDto.isNotLocked()) {
            throw new ServiceValidationException(SHIFT_STATUS + shiftPlanningExecutionDto.getStatus() + "] and can't be UNLOCKED !!! ");
        }
        shiftPlanningExecutionDto.markCompleted();
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Déverouillage du shift " + "'" + shiftPlanningExecutionDto.getShift().getName() + "'" + FOR_STATION + "'" + station.getName() + "'" + " pour le compte client " + "'" + station.getCustomerAccountName() + "'",
                (User) authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return update(shiftPlanningExecutionDto);
    }

    /**
     * 1. Mise à jour pompiste =>
     * a. Remplacement total: mettre à jour les executionDetail de la pompe pour mettre le pompiste "remplaçant" (mise à jour niveau Pompe)
     * b. cas renfort: créer une copie des executionDetail de la pompe au nom du pompiste "renfort" (mise à jour niveau Pompe)
     * 2. Mise à jour heure début/fin, mettre à jour toutes les pompes du pompiste: (mise à jour niveau pompiste)
     * 3. Mise à jour "tank return", mettre à jour l'execution detail sélectionné (mise à jour niveau Nozzle)
     *
     * @param shiftDetailNeededModifications
     * @return
     */
    @Override
    public ShiftPlanningExecutionDto updateShiftDetails(ShiftDetailUpdatesDto shiftDetailNeededModifications, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StationDto station = stationService.checkedFindById(shiftDetailNeededModifications.getStationId());
        final Optional<ShiftPlanningExecutionDto> existingShiftExecution = getDao().findByStation(shiftDetailNeededModifications.getShiftPlanningExecutionId(), shiftDetailNeededModifications.getStationId());
        final ShiftPlanningExecutionDto shiftPlanningExecutionDto = existingShiftExecution.orElseThrow(() -> new ShiftExecutionException("Unknown shift planning!"));
        final ShiftPlanningExecutionDetailDto originalShiftExecutionDetailToUpdate = shiftPlanningExecutionDto
                .getShiftPlanningExecutionDetail().stream().filter(el -> el.getId().equals(shiftDetailNeededModifications.getId()))
                .findFirst().orElseThrow(() -> new ServiceValidationException("Unknown shift planning execution detail!"));
        LocalDateTime endDateTime = shiftDetailNeededModifications.getEndDateTime();
        LocalDateTime startDateTime = shiftDetailNeededModifications.getStartDateTime();
        if (shiftPlanningExecutionDto.getShift().isCrossesDayBoundary() && endDateTime.isBefore(startDateTime)) {
            LocalDateTime crossDayEndDateTime = endDateTime.plusDays(1);
            shiftDetailNeededModifications.setEndDateTime(crossDayEndDateTime);
        }
        validateShiftDetailRequestedModifications(shiftDetailNeededModifications);
        final Long concernedPumpId = originalShiftExecutionDetailToUpdate.getPumpId();
        final Long originalPumpAttendantId = originalShiftExecutionDetailToUpdate.getPumpAttendantId();
        // Action to take
        handlePumpAttendantChange(originalShiftExecutionDetailToUpdate, shiftDetailNeededModifications, shiftPlanningExecutionDto, originalPumpAttendantId, concernedPumpId);
        ShiftPlanningExecutionDto planningExecutionDto = update(shiftPlanningExecutionDto);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Modifcation des détails du shift en cours " + "'" + shiftPlanningExecutionDto.getShift().getName() + "'" + FOR_STATION + "'" + station.getName() + "'",
                (User) authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return planningExecutionDto;
    }

    /**
     * Valide les modifications requises
     *
     * @param shiftDetailModification
     */
    public void validateShiftDetailRequestedModifications(ShiftDetailUpdatesDto shiftDetailModification) {
        final ESSValidationResult validationResult = shiftDetailUpdatesValidator.validate(shiftDetailModification);
        if (validationResult.hasErrors()) {
            throw new ServiceValidationException(validationResult.getMessage());
        }
    }

    /**
     * Intercepte les modifications suite à un changement de pompiste
     *
     * @param shiftDetailModification
     * @param shiftPlanningExecution
     * @param originalPumpAttendantId
     * @param concernedPumpId
     */
    public static void handlePumpAttendantChange(ShiftPlanningExecutionDetailDto originalShiftExecutionDetailToUpdate, ShiftDetailUpdatesDto shiftDetailModification, ShiftPlanningExecutionDto shiftPlanningExecution, Long originalPumpAttendantId, Long concernedPumpId) {
        if (shiftPlanningExecution.isLocked()) {
            throw new ServiceValidationException("Modification n'est pas disponible pour un poste verouillé");
        }
        if (shiftDetailModification.isReinforcementRequested()) {
            // Créer des shift execution details sur la pompe donnée et l'affecter au nouveau pompiste
            final List<ShiftPlanningExecutionDetailDto> newShiftExecutionDetails = shiftPlanningExecution.getPumpAttendantShiftExecutionDetails(originalPumpAttendantId)
                    .map(el -> el.cloneOnIntervalsAndApply(shiftDetailModification.getPumpAttendantId(), shiftDetailModification.getStartDateTime(), shiftDetailModification.getEndDateTime(), shiftDetailModification.getTankReturn()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            shiftPlanningExecution.addShiftPlanningExecutionDetails(newShiftExecutionDetails);
        } else {
            if (!shiftDetailModification.getPumpAttendantId().equals(originalPumpAttendantId) || (shiftDetailModification.getPumpAttendantId().equals(originalPumpAttendantId) && shiftDetailModification.getTankReturn().compareTo(originalShiftExecutionDetailToUpdate.getTankReturn()) == 0)) {
                // sur la pompe donnée, remplacer le pompiste
                shiftPlanningExecution.getShiftPlanningExecutionDetail()
                        .stream().filter(el -> el.getPumpId().equals(concernedPumpId))
                        .forEach(el -> {
                            el.setPumpAttendantId(shiftDetailModification.getPumpAttendantId());
                            el.setStartDateTime(shiftDetailModification.getStartDateTime());
                            el.setEndDateTime(shiftDetailModification.getEndDateTime());
                            el.setTankReturn(shiftDetailModification.getTankReturn());
                        });
            } else {
                originalShiftExecutionDetailToUpdate.setStartDateTime(shiftDetailModification.getStartDateTime());
                originalShiftExecutionDetailToUpdate.setEndDateTime(shiftDetailModification.getEndDateTime());
                originalShiftExecutionDetailToUpdate.setTankReturn(shiftDetailModification.getTankReturn());

            }
        }
    }
}
