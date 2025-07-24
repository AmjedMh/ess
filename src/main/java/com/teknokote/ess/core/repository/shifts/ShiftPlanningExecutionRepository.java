package com.teknokote.ess.core.repository.shifts;

import com.teknokote.ess.core.model.shifts.ShiftExecutionStatus;
import com.teknokote.ess.core.model.shifts.ShiftPlanningExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftPlanningExecutionRepository extends JpaRepository<ShiftPlanningExecution, Long> {
    @Query("SELECT e FROM ShiftPlanningExecution e WHERE e.shiftPlanning.id IN :shiftPlanningIds")
    List<ShiftPlanningExecution> findByShiftPlanningIds(List<Long> shiftPlanningIds);

    @Query("SELECT se FROM ShiftPlanningExecution se WHERE se.shift.id = :shiftId AND se.shiftPlanning.id = :shiftPlanningId")
    ShiftPlanningExecution findByDayAndStatus(Long shiftId, Long shiftPlanningId);

    @Query("SELECT se FROM ShiftPlanningExecution se WHERE se.shiftPlanning.station.id = :stationId and se.status = :status")
    ShiftPlanningExecution findInProgressExecution(Long stationId, ShiftExecutionStatus status);

    @Query("SELECT se FROM ShiftPlanningExecution se WHERE se.shiftPlanning.id = :shiftPlanningId")
    Optional<ShiftPlanningExecution> findByShiftPlanningId(Long shiftPlanningId);

    @Query("SELECT se FROM ShiftPlanningExecution se WHERE se.id = :id and se.shiftPlanning.station.id = :stationId")
    Optional<ShiftPlanningExecution> findByStation(Long id, Long stationId);

    @Query("SELECT se FROM ShiftPlanningExecution se WHERE se.shiftPlanning.station.id = :stationId and DATE(se.startDateTime) = :day")
    List<ShiftPlanningExecution> findByStationAndDate(Long stationId, LocalDate day);

    @Query("SELECT se FROM ShiftPlanningExecution se WHERE se.shiftPlanning.station.id = :stationId and se.workDayShiftPlanningExecution.workDayShiftPlanningId = :workDayId")
    List<ShiftPlanningExecution> findByWorkDayAndStation(Long stationId, Long workDayId);

    @Query("SELECT se FROM ShiftPlanningExecution se WHERE se.workDayShiftPlanningExecution.workDayShiftPlanningId = :workDayId")
    List<ShiftPlanningExecution> findByWorkDay(Long workDayId);

    @Modifying
    @Query("delete from ShiftPlanningExecution spe where spe.shiftPlanningId in (:shiftPlanningIds)")
    void deleteForPlannings(List<Long> shiftPlanningIds);
}
