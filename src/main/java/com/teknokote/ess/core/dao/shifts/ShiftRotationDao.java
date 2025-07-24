package com.teknokote.ess.core.dao.shifts;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.dto.PeriodDto;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ShiftRotationDao extends BasicDao<Long, ShiftRotationDto>
{
   List<ShiftRotationDto> findAllByStation(Long stationId);

   ShiftRotationDto findValidRotation(Long stationId, LocalDate month);
   ShiftRotationDto findRotationForStation(String ptsId, LocalDate month);

   PeriodDto dailyPeriodForStation(String ptsId, LocalDateTime dateTime);
}

