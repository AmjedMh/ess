package com.teknokote.ess.core.dao.impl.shifts;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.shifts.ShiftRotationMapper;
import com.teknokote.ess.core.dao.shifts.ShiftRotationDao;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.core.model.shifts.ShiftRotation;
import com.teknokote.ess.core.repository.shifts.ShiftRotationRepository;
import com.teknokote.ess.dto.PeriodDto;
import com.teknokote.ess.dto.ShiftDto;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@Getter
@Setter
public class ShiftRotationDaoImpl extends JpaGenericDao<Long, ShiftRotationDto, ShiftRotation> implements ShiftRotationDao {
    @Autowired
    private ShiftRotationMapper mapper;
    @Autowired
    private ShiftRotationRepository repository;

    @Override
    public List<ShiftRotationDto> findAllByStation(Long stationId) {
        return getRepository().findAllByStation(stationId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }

    @Override
    public ShiftRotationDto findValidRotation(Long stationId, LocalDate month) {
        final List<ShiftRotationDto> collect = getRepository().findValidRotation(stationId, month).stream().map(getMapper()::toDto).toList();
        return CollectionUtils.isEmpty(collect) ? null : collect.get(0);
    }
    @Override
    public ShiftRotationDto findRotationForStation(String ptsId, LocalDate month) {
        return getMapper().toDto(getRepository().findRotationForStation(ptsId, month));
    }


    @Override
    public PeriodDto dailyPeriodForStation(String stationId,LocalDateTime dateTime) {
        ShiftRotationDto rotation = findRotationForStation(stationId, dateTime.toLocalDate());
        LocalDateTime todayDayStart = dateTime.toLocalDate().atTime(0, 0, 0);
        LocalDateTime todayDayEnd = dateTime.toLocalDate().atTime(23, 59, 59);
        if (Objects.nonNull(rotation)) {
            ShiftDto firstShitInRotation = rotation.firstShift();
            ShiftDto lastShiftInRotation = rotation.lastShift();
            todayDayStart = dateTime.toLocalDate().atTime(firstShitInRotation.getStartingTime());
            todayDayEnd = lastShiftInRotation.isCrossesDayBoundary() ?
                    dateTime.toLocalDate().atTime(lastShiftInRotation.getEndingTime()).plusDays(1) :
                    dateTime.toLocalDate().atTime(lastShiftInRotation.getEndingTime());
        }
        // Construct periods based on today's period.
        return PeriodDto.today(todayDayStart, todayDayEnd);
    }

    @Override
    protected ShiftRotation beforeCreate(ShiftRotation shiftRotation, ShiftRotationDto dto) {
        shiftRotation.setStation(getEntityManager().getReference(Station.class, dto.getStationId()));
        ShiftRotation savedShiftRotation = super.beforeCreate(shiftRotation, dto);
        savedShiftRotation.getShifts().forEach(shift -> shift.setShiftRotation(savedShiftRotation));
        return savedShiftRotation;
    }

    @Override
    protected ShiftRotation beforeUpdate(ShiftRotation shiftRotation, ShiftRotationDto dto) {

        if (Objects.nonNull(dto.getShifts())) {
            shiftRotation.getShifts().forEach(shift -> shift.setShiftRotation(shiftRotation));
        }
        return super.beforeUpdate(shiftRotation, dto);
    }
}
