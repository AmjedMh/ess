package com.teknokote.ess.core.repository.shifts;

import com.teknokote.ess.core.model.shifts.WorkDayShiftPlanning;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkDayShiftPlanningRepository extends JpaRepository<WorkDayShiftPlanning, Long>
{
   @Query("SELECT ws FROM WorkDayShiftPlanning ws WHERE ws.station.id = :stationId and ws.day = :day")
   WorkDayShiftPlanning findByStationAndDay(Long stationId, LocalDate day);

   @Modifying
   @Query("delete from WorkDayShiftPlanning spe where spe.id in (:workDayIds)")
   void deleteForPlannings(List<Long> workDayIds);

   @Query("SELECT count(ws) FROM WorkDayShiftPlanning ws" + " join ws.shiftPlannings sp WHERE ws.shiftRotationId = :shiftRotationId and sp.hasExecution")
   Long hasExecutionsForRotation(Long shiftRotationId);

   @Query("SELECT ws FROM WorkDayShiftPlanning ws WHERE ws.station.id = :stationId and ws.shiftRotation.id = :shiftRotationId")
   List<WorkDayShiftPlanning> findByStationAndRotation(Long stationId, Long shiftRotationId);
}
