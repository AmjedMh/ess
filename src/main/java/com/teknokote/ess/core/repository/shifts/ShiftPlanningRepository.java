package com.teknokote.ess.core.repository.shifts;

import com.teknokote.ess.core.model.shifts.ShiftPlanning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftPlanningRepository extends JpaRepository<ShiftPlanning, Long> {
    @Query("SELECT sp FROM ShiftPlanning sp WHERE sp.station.id = :stationId")
    List<ShiftPlanning> findAllByStation(Long stationId);

    @Query("SELECT sp FROM ShiftPlanning sp WHERE sp.station.id = :stationId and (sp.workDayShiftPlanning.day between :startDate and :endDate)")
    List<ShiftPlanning> findByStationAndPeriod(Long stationId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT sp FROM ShiftPlanning sp WHERE sp.station.id = :stationId and sp.workDayShiftPlanning.day = :day")
    List<ShiftPlanning> findByStationAndDay(Long stationId, LocalDate day);

    @Query("SELECT sp FROM ShiftPlanning sp WHERE sp.station.id = :stationId and sp.shiftRotation.id = :shiftRotationId")
    List<ShiftPlanning> findByStationAndRotation(Long stationId, Long shiftRotationId);

    @Query("SELECT DISTINCT sp.pumpAttendantTeamId FROM ShiftPlanning sp WHERE sp.shiftRotationId = :shiftRotationId AND sp.stationId = :stationId")
    List<Long> findTeamIdsByStationAndShiftRotation(Long stationId, Long shiftRotationId);
}