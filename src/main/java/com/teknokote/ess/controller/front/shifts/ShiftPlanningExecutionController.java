package com.teknokote.ess.controller.front.shifts;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.shifts.ShiftPlanningExecutionService;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningExecutionDto;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDto;
import com.teknokote.ess.dto.shifts.ShiftDetailUpdatesDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.SHIFT_PLANNING_EXECUTION_ROOT)
public class ShiftPlanningExecutionController {
    @Autowired
    private ShiftPlanningExecutionService shiftPlanningExecutionService;

    @PostMapping(EndPoints.GENERATE)
    public ResponseEntity<List<DynamicShiftPlanningExecutionDto>> generateShiftPlanningExecution(@PathVariable Long stationId,
                                                                                                 @RequestParam LocalDate day) {
        List<DynamicShiftPlanningExecutionDto> dailyShiftPlanningExecutions = shiftPlanningExecutionService.generate(day, stationId);
        return new ResponseEntity<>(dailyShiftPlanningExecutions, HttpStatus.CREATED);
    }

    @PostMapping(EndPoints.START)
    public ResponseEntity<ShiftPlanningExecutionDto> startShiftPlanningExecution(@PathVariable Long stationId,
                                                                                 @RequestParam Long shiftPlanningExecutionId,
                                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime, HttpServletRequest request) {
        ShiftPlanningExecutionDto shiftPlanningExecutionDetailDto = shiftPlanningExecutionService.start(stationId, shiftPlanningExecutionId, startDateTime,request);
        return new ResponseEntity<>(shiftPlanningExecutionDetailDto, HttpStatus.CREATED);
    }

    @PostMapping(EndPoints.STOP)
    public ResponseEntity<ShiftPlanningExecutionDto> stopShiftPlanningExecution(@PathVariable Long stationId,
                                                                                @RequestParam Long shiftPlanningExecutionId,
                                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,HttpServletRequest request) {
        ShiftPlanningExecutionDto shiftPlanningExecutionDetailDto = shiftPlanningExecutionService.stop(stationId, shiftPlanningExecutionId, endDateTime,request);
        return new ResponseEntity<>(shiftPlanningExecutionDetailDto, HttpStatus.CREATED);
    }

    @PostMapping(EndPoints.UNLOCK)
    public ResponseEntity<ShiftPlanningExecutionDto> unlockShiftPlanningExecution(@PathVariable Long stationId,@RequestParam Long shiftPlanningExecutionId,HttpServletRequest request) {
        ShiftPlanningExecutionDto shiftPlanningExecutionDetailDto = shiftPlanningExecutionService.unlock(stationId,shiftPlanningExecutionId,request);
        return new ResponseEntity<>(shiftPlanningExecutionDetailDto, HttpStatus.CREATED);
    }

    @PostMapping(EndPoints.LOCK)
    public ResponseEntity<ShiftPlanningExecutionDto> lockShiftPlanningExecution(@PathVariable Long stationId,@RequestParam Long shiftPlanningExecutionId,HttpServletRequest request) {
        ShiftPlanningExecutionDto shiftPlanningExecutionDetailDto = shiftPlanningExecutionService.lock(stationId,shiftPlanningExecutionId,request);
        return new ResponseEntity<>(shiftPlanningExecutionDetailDto, HttpStatus.CREATED);
    }

    @PostMapping(EndPoints.UPDATE_SHEET)
    public ResponseEntity<PumpAttendantCollectionSheetDto> createPumpAttendantSheet(@RequestBody PumpAttendantCollectionSheetDto pumpAttendantCollectionSheetDto,HttpServletRequest request) {
        PumpAttendantCollectionSheetDto createdPumpAttendantCollectionSheetDto = shiftPlanningExecutionService.updateCollectionSheetForPumpAttendant(pumpAttendantCollectionSheetDto,request);
        return new ResponseEntity<>(createdPumpAttendantCollectionSheetDto, HttpStatus.CREATED);
    }

    @PostMapping(EndPoints.UPDATE)
    public ResponseEntity<ShiftPlanningExecutionDto> updateShiftPlanningExecution(@RequestBody ShiftPlanningExecutionDto dto) {
        ShiftPlanningExecutionDto savedShiftPlanningExecution = shiftPlanningExecutionService.update(dto);
        return new ResponseEntity<>(savedShiftPlanningExecution, HttpStatus.CREATED);
    }

    @PostMapping(EndPoints.UPDATE_DETAILS)
    public ResponseEntity<ShiftPlanningExecutionDto> updateShiftPlanningExecutionDetail(@PathVariable Long stationId, @RequestBody ShiftDetailUpdatesDto dto,HttpServletRequest request) {
        dto.setStationId(stationId);
        ShiftPlanningExecutionDto savedShiftPlanningExecution = shiftPlanningExecutionService.updateShiftDetails(dto,request);
        return new ResponseEntity<>(savedShiftPlanningExecution, HttpStatus.OK);
    }


    @GetMapping(EndPoints.INFO)
    public ResponseEntity<ShiftPlanningExecutionDto> getShiftPlanningExecution(@PathVariable Long id) {
        ShiftPlanningExecutionDto foundShiftPlanningExecution = shiftPlanningExecutionService.checkedFindById(id);
        return new ResponseEntity<>(foundShiftPlanningExecution, HttpStatus.CREATED);
    }

    @GetMapping
    public List<ShiftPlanningExecutionDto> listShiftPlanningExecution() {
        return shiftPlanningExecutionService.findAll();
    }

    @GetMapping(EndPoints.PUMP_ATTENDANT_SALES)
    public BigDecimal calculateTotalAmountPumpAttendant(@PathVariable Long id,
                                                        @RequestParam Long pumpAttendantId) {
        return shiftPlanningExecutionService.calculateTotalAmountPumpAttendant(id, pumpAttendantId);
    }
}
