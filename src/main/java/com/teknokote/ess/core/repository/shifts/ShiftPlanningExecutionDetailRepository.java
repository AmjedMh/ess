package com.teknokote.ess.core.repository.shifts;

import com.teknokote.ess.core.model.shifts.ShiftPlanningExecutionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ShiftPlanningExecutionDetailRepository extends JpaRepository<ShiftPlanningExecutionDetail, Long>
{
    @Query("SELECT se FROM ShiftPlanningExecutionDetail se WHERE se.shiftPlanningExecution.id = :shiftPlanningExecutionId AND se.pumpAttendant.id = :pumpAttendantId")
    List<ShiftPlanningExecutionDetail> findByShiftPlanningExecutionIdAndPumpAttendantId(Long shiftPlanningExecutionId, Long pumpAttendantId);
    @Query("SELECT se FROM ShiftPlanningExecutionDetail se WHERE se.shiftPlanningExecution.id = :shiftPlanningExecutionDetailId AND se.pumpAttendant.id = :pumpAttendantId AND se.pump.id = :pumpId")
    List<ShiftPlanningExecutionDetail> findByShiftPlanningExecutionDetailIdAndPumpAttendantIdAndPumpId(Long shiftPlanningExecutionDetailId, Long pumpAttendantId, Long pumpId);
    @Query("SELECT se FROM ShiftPlanningExecutionDetail se WHERE se.id = :id AND se.shiftPlanningExecution.shiftPlanning.station.id = :stationId")
    ShiftPlanningExecutionDetail findByStation(Long id, Long stationId);

    @Modifying
    @Query("delete from ShiftPlanningExecutionDetail sped where sped.shiftPlanningExecution.id " +
       "in (select spe.id from ShiftPlanningExecution spe where spe.shiftPlanning.id in :shiftPlanningIds) ")
    void deleteForPlannings(List<Long> shiftPlanningIds);
}
