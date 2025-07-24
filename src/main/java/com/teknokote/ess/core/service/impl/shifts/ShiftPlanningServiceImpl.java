package com.teknokote.ess.core.service.impl.shifts;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.dao.StationDao;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningDao;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningExecutionDao;
import com.teknokote.ess.core.dao.shifts.WorkDayShiftPlanningDao;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.ShiftService;
import com.teknokote.ess.core.service.organization.PumpAttendantTeamService;
import com.teknokote.ess.core.service.shifts.ShiftPlanningService;
import com.teknokote.ess.core.service.shifts.ShiftRotationService;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningService;
import com.teknokote.ess.dto.ShiftDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningDto;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;
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

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.IntStream;

@Service
@Getter
@Setter
@Transactional(readOnly = true)
public class ShiftPlanningServiceImpl extends GenericCheckedService<Long, ShiftPlanningDto> implements ShiftPlanningService {
    @Autowired
    private ESSValidator<ShiftPlanningDto> validator;
    @Autowired
    private ShiftPlanningDao dao;
    @Autowired
    private WorkDayShiftPlanningDao workDayShiftPlanningDao;
    @Autowired
    private ShiftPlanningExecutionDao executionDao;
    @Autowired
    private PumpAttendantTeamService pumpAttendantTeamService;
    @Autowired
    private ShiftRotationService shiftRotationService;
    @Autowired
    private ShiftService shiftService;
    @Autowired
    private WorkDayShiftPlanningService workDayShiftPlanningService;
    @Autowired
    private StationDao stationDao;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Hypothèses:
     * - Nombre de postes = nombre d'équipes -1
     */
    @Override
    @Transactional
    public List<ShiftPlanningDto> generatePlanning(Long stationId, LocalDate month, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<StationDto> station = stationDao.findById(stationId);

        // Check if the station is present before accessing its value
        if (!station.isPresent()) {
            throw new EntityNotFoundException("Station not found with ID: " + stationId);
        }

        ShiftRotationDto rotation = shiftRotationService.findValidRotation(stationId, month);
        List<ShiftPlanningDto> existingPlanning = findByStationAndMonth(stationId, month);

        if (CollectionUtils.isNotEmpty(existingPlanning)) {
            deletePlanning(existingPlanning);
            deleteWorkDays(existingPlanning);
            EntityActionEvent event = new EntityActionEvent(
                    this,
                    "Réuinitialisation  du shift planning por  la rotation " + "'" + rotation.getName() + "'" + " pour la station " + "'" + station.get().getName() + "'" + " por le compte client " + "'" + station.get().getCustomerAccountName() + "'",
                    (User) authentication.getPrincipal(),
                    request
            );
            eventPublisher.publishEvent(event);
        }

        if (Objects.nonNull(rotation)) {
            existingPlanning = generate(stationId, rotation.getStartValidityDate(), rotation.getEndValidityDate(), rotation.getNbrOffDays());
            EntityActionEvent event = new EntityActionEvent(
                    this,
                    "generate  shift planning for the rotation " + "'" + rotation.getName() + "'" + " for station " + "'" + station.get().getName() + "'" + " for customerAccount " + "'" + station.get().getCustomerAccountName() + "'",
                    (User) authentication.getPrincipal(),
                    request
            );
            eventPublisher.publishEvent(event);
        }

        return existingPlanning;
    }

    @Transactional
    @Override
    public List<ShiftPlanningDto> generate(Long stationId, LocalDate startDate, LocalDate endDate, Integer numberOffDays) {
        final List<ShiftPlanningDto> existingShiftPlanningsForTheMonth = getDao().findByStationAndPeriod(stationId, startDate, endDate);
        if (CollectionUtils.isNotEmpty(existingShiftPlanningsForTheMonth)) {
            deletePlanning(existingShiftPlanningsForTheMonth);
        }
        if (startDate.getDayOfMonth()!=1){
            throw new ServiceValidationException("Start Validity Date of rotation  must be the first day of the month !!");
        }
        // Generate
        final ShiftRotationDto rotation = shiftRotationService.findValidRotation(stationId, startDate);
        final List<PumpAttendantTeamDto> teams = pumpAttendantTeamService.findByRotation(stationId, rotation.getId());
        ShiftDto offDayShift = shiftService.findOffDay();

        final List<ShiftDto> shifts = new ArrayList<>(rotation.getShifts());
        // Add off day shift
        shifts.add(offDayShift);
        int nbPost = shifts.size();
        if(teams.size()<nbPost){
            // complete the teams with empty ones
            while(teams.size()<nbPost){
                teams.add(null);
            }
        }

        Map<Integer, List<PumpAttendantTeamDto>> planning = new HashMap<>();
        int freeDayIndex = 1;
        int changeIndex = 1;
        // init first day configuration
        planning.put(1, new ArrayList<>(teams));
        List<ShiftPlanningDto> result = new ArrayList<>(generateShiftPlannings(rotation, shifts, planning.get(1), startDate));
        for (LocalDate date = startDate.plusDays(1); !date.isAfter(endDate); date = date.plusDays(1)) {
            int dayOfMonth = date.getDayOfMonth();
            List<PumpAttendantTeamDto> lastDayPlanning;
            if (date.getDayOfMonth() == 1) {
                // If it's the first day of the month, use the last day of the previous month
                lastDayPlanning = new ArrayList<>(planning.get(date.minusDays(1).getDayOfMonth()));
            } else {
                lastDayPlanning = new ArrayList<>(planning.get(dayOfMonth - 1));
            }
            freeDayIndex++;
            if (freeDayIndex > numberOffDays) {
                // change planning
                PumpAttendantTeamDto freeTeam = lastDayPlanning.get(nbPost - 1);
                lastDayPlanning.set(nbPost - 1, lastDayPlanning.get(changeIndex - 1));
                lastDayPlanning.set(changeIndex - 1, freeTeam);
                //update change index
                changeIndex++;
                if (changeIndex > nbPost - 1) changeIndex = 1;
                // increment freeday count
                freeDayIndex = 1;
            }
            planning.put(dayOfMonth, new ArrayList<>(lastDayPlanning));
            // Generate ShiftPlanningDto for the current date
            result.addAll(generateShiftPlannings(rotation, shifts, planning.get(dayOfMonth), date));
        }

        return result;
    }

    public List<ShiftPlanningDto> generateShiftPlannings(
            ShiftRotationDto rotation,
            List<ShiftDto> shifts,
            List<PumpAttendantTeamDto> lastDayPlanning,
            LocalDate date
    ) {
        int nbPost = shifts.size();
        // Create new WorkDayShiftPlanningDto for the provided date
        WorkDayShiftPlanningDto workDayShiftPlanning = WorkDayShiftPlanningDto.builder()
                .stationId(rotation.getStationId())
                .shiftRotationId(rotation.getId())
                .day(date)
                .build();
        WorkDayShiftPlanningDto workDay = workDayShiftPlanningDao.create(workDayShiftPlanning);
        return IntStream.range(0, nbPost - 1)
                .mapToObj(i -> ShiftPlanningDto.builder()
                        .shiftId(shifts.get(i).getId())
                        .shiftRotationId(rotation.getId())
                        .pumpAttendantTeamId(Objects.nonNull(lastDayPlanning.get(i)) ? lastDayPlanning.get(i).getId() : null)
                        .stationId(rotation.getStationId())
                        .hasExecution(Boolean.FALSE)
                        .workDayShiftPlanningId(workDay.getId())
                        .index(i)
                        .build())
                .toList();
    }

    /**
     * Deletes an passed shift plannings
     *
     * @param shiftPlannings
     */
    public void deletePlanning(List<ShiftPlanningDto> shiftPlannings) {
        try {
            getDao().deleteAllByIdInBatch(shiftPlannings.stream().map(ShiftPlanningDto::getId).toList());
        } catch (Exception ex) {
            throw new ServiceValidationException("Could not delete shift planning! Check if an existing shift planning execution is running.");
        }
    }

    @Override
    public List<DynamicShiftPlanningDto> getDynamicShiftPlannig(Long stationId) {
        List<ShiftPlanningDto> shiftPlanningDtos = getDao().findAllByStation(stationId);
        return shiftPlanningDtos.stream()
                .map(this::mapToDynamicDto)
                .toList();
    }

    public DynamicShiftPlanningDto mapToDynamicDto(ShiftPlanningDto shiftPlanningDto) {
        WorkDayShiftPlanningDto workDayShiftPlanning = workDayShiftPlanningService.checkedFindById(shiftPlanningDto.getWorkDayShiftPlanningId());

        // Extracting the offDay logic into a separate variable
        Boolean offDay = null;
        if (shiftPlanningDto.getShift() != null) {
            offDay = Objects.nonNull(shiftPlanningDto.getShift().getOffDay()) ? shiftPlanningDto.getShift().getOffDay() : false;
        }

        // Extracting the shiftName logic into a separate variable
        String shiftName = shiftPlanningDto.getShift() != null ? shiftPlanningDto.getShift().getName() : null;

        // Extracting the shiftRotationName logic into a separate variable
        String shiftRotationName = shiftPlanningDto.getShiftRotation() != null ? shiftPlanningDto.getShiftRotation().getName() : null;

        // Building the DynamicShiftPlanningDto
        DynamicShiftPlanningDto.DynamicShiftPlanningDtoBuilder builder = DynamicShiftPlanningDto.builder()
                .id(shiftPlanningDto.getId())
                .shiftId(shiftPlanningDto.getShiftId())
                .shiftIndex(shiftPlanningDto.getShift() != null ? shiftPlanningDto.getIndex() : null)
                .offDay(offDay)
                .day(workDayShiftPlanning.getDay())
                .hasExecution(shiftPlanningDto.getHasExecution())
                .shiftName(shiftName)
                .shiftRotationId(shiftPlanningDto.getShiftRotationId())
                .shiftRotationName(shiftRotationName);

        if (shiftPlanningDto.getPumpAttendantTeam() != null) {
            builder.pumpAttendantTeam(shiftPlanningDto.getPumpAttendantTeam())
                    .pumpAttendantTeamName(shiftPlanningDto.getPumpAttendantTeam().getName());
        }

        return builder.build();
    }

    @Override
    public List<ShiftPlanningDto> findByStationAndMonth(Long stationId, LocalDate month) {
        LocalDate startDate = month.withDayOfMonth(1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        return getDao().findByStationAndPeriod(stationId, startDate, endDate);
    }

    @Override
    public List<ShiftPlanningDto> findByStationAndDay(Long stationId, LocalDate day) {
        return getDao().findByStationAndDay(stationId, day);
    }

    @Override
    public List<PumpAttendantTeamDto> findTeamsByStationAndShiftRotation(Long stationId, Long shiftRotationId) {
        return pumpAttendantTeamService.findByIdsIn(getDao().findTeamIdsByStationAndShiftRotation(stationId, shiftRotationId));
    }


    @Override
    @Transactional
    public List<ShiftPlanningDto> updateList(List<ShiftPlanningDto> dto) {
        return getDao().updateList(dto);
    }

    @Override
    public void markAsExecuting(Long shiftPlanningId)
    {
        final ShiftPlanningDto shiftPlanning = checkedFindById(shiftPlanningId);
        shiftPlanning.setHasExecution(true);
        getDao().update(shiftPlanning);
    }
    @Override
    @Transactional
    public List<ShiftPlanningDto> resetShiftPlanning(Long stationId, LocalDate month,HttpServletRequest request)
    {
        List<ShiftPlanningDto> shiftPlanningList = findByStationAndMonth(stationId, month);
        if(CollectionUtils.isEmpty(shiftPlanningList)) return List.of();
        // Check if any of the shift plannings to be reset have executions
        boolean existPlanningWithExecutions = shiftPlanningList.stream()
           .anyMatch(ShiftPlanningDto::getHasExecution);
        if(existPlanningWithExecutions)
        {
            throw new ServiceValidationException("You can't reinitialise this shift planning because there are one or more running executions. You need to create another rotation");
        }
        List<Long> shiftPlanningIds = shiftPlanningList.stream()
           .map(ShiftPlanningDto::getId)
           .toList();
        // Delete generated executions
        executionDao.deleteForPlannings(shiftPlanningIds);
        getDao().deleteAllByIdInBatch(shiftPlanningIds);
        return createList(generatePlanning(stationId, month,request));
    }

    @Transactional
    @Override
    public void deleteShiftPlanning(Long stationId, LocalDate month){
        List<ShiftPlanningDto> shiftPlanningList = findByStationAndMonth(stationId, month);
        if(CollectionUtils.isEmpty(shiftPlanningList)) return;
        // Check if any of the shift plannings to be reset have executions
        boolean existPlanningWithExecutions = shiftPlanningList.stream()
           .anyMatch(ShiftPlanningDto::getHasExecution);
        if(existPlanningWithExecutions)
        {
            throw new ServiceValidationException("You can't reinitialise this shift planning because there are one or more running executions. You need to create another rotation");
        }
        List<Long> shiftPlanningIds = shiftPlanningList.stream()
           .map(ShiftPlanningDto::getId)
           .toList();
        executionDao.deleteForPlannings(shiftPlanningIds);
    }

    public void deleteWorkDays(List<ShiftPlanningDto> shiftPlannings){
        List<Long> workDayIds = shiftPlannings.stream()
                .map(ShiftPlanningDto::getWorkDayShiftPlanningId)
                .toList();
        workDayShiftPlanningDao.deleteForPlanning(workDayIds);
    }
}
