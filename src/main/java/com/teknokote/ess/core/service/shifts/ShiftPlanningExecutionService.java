package com.teknokote.ess.core.service.shifts;

import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningExecutionDto;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDto;
import com.teknokote.ess.dto.shifts.ShiftDetailUpdatesDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDto;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The ShiftPlanningExecutionService interface represents a service for managing shift planning executions.
 * It provides methods for generating shift planning executions and starting the execution of shift planning.
 */
public interface ShiftPlanningExecutionService extends BaseService<Long, ShiftPlanningExecutionDto> {
    /**
     * Generates a list of executions for the given day and station ID.
     *
     * @param day       the date for which to generate the shift planning execution
     * @param stationId the ID of the station
     * @return a list of DynamicShiftPlanningExecutionDto objects
     */
    List<DynamicShiftPlanningExecutionDto> generate(LocalDate day, Long stationId);

    /**
     * Starts the execution of shift planning.
     *
     * @param stationId                the ID of the station
     * @param shiftPlanningExecutionId the ID of the shift planning execution
     * @param startDateTime            the start date and time of the execution
     * @return the ShiftPlanningExecutionDto with the updated start date and time*
     */
    ShiftPlanningExecutionDto start(Long stationId, Long shiftPlanningExecutionId, LocalDateTime startDateTime, HttpServletRequest request);

    ShiftPlanningExecutionDto stop(Long stationId, Long shiftPlanningExecutionId, LocalDateTime endDateTime,HttpServletRequest request);

    ShiftPlanningExecutionDto unlock(Long stationId,Long shiftPlanningExecutionId,HttpServletRequest request);

    ShiftPlanningExecutionDto lock(Long stationId,Long shiftPlanningExecutionId,HttpServletRequest request);

    List<ShiftPlanningExecutionDto> findExistingShiftPlanningExecutions(Long stationId, Long workDayId);

    PumpAttendantCollectionSheetDto updateCollectionSheetForPumpAttendant(PumpAttendantCollectionSheetDto pumpAttendantCollectionSheetDto,HttpServletRequest request);

    BigDecimal calculateTotalAmountPumpAttendant(Long shiftPlanningExecutionId, Long pumpAttendantId);

    /**
     * Modification des execution détails d'un shift
     *
     * @param shiftDetailNeededModifications
     * @return
     */
    ShiftPlanningExecutionDto updateShiftDetails(ShiftDetailUpdatesDto shiftDetailNeededModifications,HttpServletRequest request);
}
