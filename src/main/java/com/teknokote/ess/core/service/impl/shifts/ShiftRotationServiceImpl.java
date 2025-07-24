package com.teknokote.ess.core.service.impl.shifts;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.dao.organization.PumpAttendantTeamDao;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningDao;
import com.teknokote.ess.core.dao.shifts.ShiftRotationDao;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.impl.StationService;
import com.teknokote.ess.core.service.shifts.ShiftRotationService;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningService;
import com.teknokote.ess.dto.PeriodDto;
import com.teknokote.ess.dto.ShiftDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import com.teknokote.ess.http.logger.EntityActionEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Data
public class ShiftRotationServiceImpl extends GenericCheckedService<Long, ShiftRotationDto> implements ShiftRotationService {
    @Autowired
    private ESSValidator<ShiftRotationDto> validator;
    @Autowired
    private ShiftRotationDao dao;
    @Autowired
    private StationService stationService;
    @Autowired
    private ShiftPlanningDao shiftPlanningDao;
    @Autowired
    private WorkDayShiftPlanningService workDayShiftPlanningService;
    @Autowired
    private PumpAttendantTeamDao pumpAttendantTeamDao;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public ShiftRotationDto create(ShiftRotationDto shiftRotationDto) {
        getValidator().validate(shiftRotationDto);
        Long stationId = shiftRotationDto.getStationId();
        stationService.checkedFindById(stationId);
        List<ShiftRotationDto> existingShiftRotations = findAllByStation(stationId);
        for (ShiftRotationDto existingRotation : existingShiftRotations) {
            if (isOverlap(shiftRotationDto, existingRotation)) {
                throw new ServiceValidationException("Overlap with existing shift rotation" + existingRotation.getName());
            }
        }
        shiftOverlap(shiftRotationDto);
        shiftTimeValidation(shiftRotationDto);
        return getDao().create(shiftRotationDto);
    }

    public boolean isOverlap(ShiftRotationDto newRotation, ShiftRotationDto existingRotation) {
        LocalDate newStartValidity = newRotation.getStartValidityDate();
        LocalDate newEndValidity = newRotation.getEndValidityDate();
        LocalDate existingStartValidity = existingRotation.getStartValidityDate();
        LocalDate existingEndValidity = existingRotation.getEndValidityDate();

        return (newStartValidity.isBefore(existingEndValidity) || newStartValidity.isEqual(existingEndValidity))
                && (newEndValidity.isAfter(existingStartValidity) || newEndValidity.isEqual(existingStartValidity));
    }

    public boolean isShiftOverlap(ShiftDto firstShift, ShiftDto secondShift) {
        LocalTime end1 = firstShift.getEndingTime();
        LocalTime start2 = secondShift.getStartingTime();

        return start2.isBefore(end1);
    }

    public void shiftOverlap(ShiftRotationDto shiftRotationDto) {
        List<ShiftDto> shifts = shiftRotationDto.getShifts();
        for (int i = 0; i < shifts.size(); i++) {
            for (int j = i + 1; j < shifts.size(); j++) {
                if (isShiftOverlap(shifts.get(i), shifts.get(j))) {
                    throw new ServiceValidationException("Overlap between shift " + shifts.get(i).getName() + " and " + shifts.get(j).getName());
                }
            }
        }
        if (shifts.size() > 1 && shifts.get(shifts.size() - 1).isCrossesDayBoundary() && isShiftOverlap(shifts.get(shifts.size() - 1), shifts.get(0))) {
            throw new ServiceValidationException("Overlap between shift " + shifts.get(shifts.size() - 1).getName() + " and " + shifts.get(0).getName());
        }
    }

    public void shiftTimeValidation(ShiftRotationDto shiftRotationDto) {
        for (ShiftDto shiftDto : shiftRotationDto.getShifts()) {
            if (!shiftDto.isCrossesDayBoundary()) {
                if (!shiftDto.getStartingTime().isBefore(shiftDto.getEndingTime())) {
                    throw new ServiceValidationException("Invalid shift timing on shift " + shiftDto.getName() + ": startingTime must be before endingTime");
                }
            } else {
                if (!shiftDto.getEndingTime().isBefore(shiftDto.getStartingTime())) {
                    throw new ServiceValidationException("Invalid shift timing on shift " + shiftDto.getName() + ": shift must be less than 24 hours");
                }
            }
        }
    }

    @Override
    public List<ShiftRotationDto> findAllByStation(Long stationId) {
        stationService.checkedFindById(stationId);
        return getDao().findAllByStation(stationId);
    }

    @Override
    public ShiftRotationDto findValidRotation(Long stationId, LocalDate month) {
        stationService.checkedFindById(stationId);
        return getDao().findValidRotation(stationId, month);
    }

    @Override
    public ShiftRotationDto addShiftRotation(Long stationId, ShiftRotationDto shiftRotationDto, HttpServletRequest request) {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        getValidator().validate(shiftRotationDto);
        StationDto stationDto = stationService.checkedFindById(stationId);
        List<ShiftRotationDto> existingShiftRotations = findAllByStation(stationId);
        for (ShiftRotationDto existingRotation : existingShiftRotations) {
            if (isOverlap(shiftRotationDto, existingRotation)) {
                throw new ServiceValidationException("Overlap with existing shift rotation" + existingRotation.getName());
            }
        }
        shiftOverlap(shiftRotationDto);
        shiftTimeValidation(shiftRotationDto);
        ShiftRotationDto shiftRotation= super.create(shiftRotationDto);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Ajout  rotation "+"'"+shiftRotation.getName()+"'"+ " pour la station " +"'"+ stationDto.getName()+"'" +" pour le  compte client "+"'"+stationDto.getCustomerAccountName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return shiftRotation;
    }

    /**
     * Cette méthode renvoie la liste des définitions des périodes (today, yesterday, week, month, year) en fonction
     * de la rotation active à la date du jour.
     * si aucune rotation n'est définie, la journée actuelle est considérée de 0h à 23h59
     *
     * @param stationId
     * @return
     */
    public List<PeriodDto> listPeriodTypesForStation(Long stationId) {
        ShiftRotationDto rotation = findValidRotation(stationId, LocalDate.now());
        List<PeriodDto> periods = new ArrayList<>();
        LocalDateTime todayDayStart = LocalDate.now().atTime(0, 0, 0);
        LocalDateTime todayDayEnd = LocalDate.now().atTime(23, 59, 59);
        if (Objects.nonNull(rotation)) {
            ShiftDto firstShitInRotation = rotation.firstShift();
            ShiftDto lastShiftInRotation = rotation.lastShift();
            todayDayStart = LocalDate.now().atTime(firstShitInRotation.getStartingTime());
            todayDayEnd = lastShiftInRotation.isCrossesDayBoundary() ?
                    LocalDate.now().atTime(lastShiftInRotation.getEndingTime()).plusDays(1) :
                    LocalDate.now().atTime(lastShiftInRotation.getEndingTime());
        }
        // Construct periods based on today's period.
        final PeriodDto today = PeriodDto.today(todayDayStart, todayDayEnd);
        periods.add(today);
        periods.add(today.yesterday());
        periods.add(today.week());
        periods.add(today.month());
        periods.add(today.year());
        return periods;
    }

    /**
     * La suppression d'une rotation implique:
     *  - la suppression des work day shift planning associés (et des plannings)
     *  - la suppression des work day shift planning executions associés (et des executions)
     *  - la suppression des teams liés
     */
    @Transactional
    @Override
    public void deleteShiftRotation(Long stationId, Long shiftRotationId,HttpServletRequest request)
    {
        Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
        StationDto stationDto=stationService.checkedFindById(stationId);
        ShiftRotationDto shiftRotation = checkedFindById(shiftRotationId);
        final boolean hasExecutionsForRotation = workDayShiftPlanningService.hasExecutionsForRotation(shiftRotationId);
        if(hasExecutionsForRotation)
        {
            throw new ServiceValidationException("La suppression de la rotation n'est pas autorisée. Des exécutions ont eu lieu pendant la période définie de cette rotation. Veuillez vérifier et réessayer");
        }
        workDayShiftPlanningService.deleteByStationAndRotation(stationId,shiftRotationId);
        pumpAttendantTeamDao.deleteForStationAndRotation(stationId,shiftRotationId);
        getDao().deleteById(shiftRotation.getId());
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Suppression  rotation "+"'"+shiftRotation.getName()+"'"+ " pour la station " +"'"+ stationDto.getName()+"'" +" pour le compte client "+"'"+stationDto.getCustomerAccountName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
    }
}
