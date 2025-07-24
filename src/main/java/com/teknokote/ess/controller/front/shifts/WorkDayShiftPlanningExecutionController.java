package com.teknokote.ess.controller.front.shifts;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningExecutionService;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningExecutionDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.WORK_DAY_SHIFT_PLANNING_EXECUTION_ROOT)
public class WorkDayShiftPlanningExecutionController {
@Autowired
private WorkDayShiftPlanningExecutionService workDayShiftPlanningExecutionService;


@PostMapping(EndPoints.ADD)
public ResponseEntity<WorkDayShiftPlanningExecutionDto> addWorkDayShiftPlanningExecution(@RequestBody WorkDayShiftPlanningExecutionDto dto) {
    WorkDayShiftPlanningExecutionDto savedWorkDayShiftPlanningExecution = workDayShiftPlanningExecutionService.create(dto);
    return new ResponseEntity<>(savedWorkDayShiftPlanningExecution, HttpStatus.CREATED);
    }


    @PutMapping(EndPoints.UPDATE)
    public ResponseEntity<WorkDayShiftPlanningExecutionDto> updateWorkDayShiftPlanningExecution(@RequestBody WorkDayShiftPlanningExecutionDto dto) {
        WorkDayShiftPlanningExecutionDto savedWorkDayShiftPlanningExecution = workDayShiftPlanningExecutionService.update(dto);
        return new ResponseEntity<>(savedWorkDayShiftPlanningExecution, HttpStatus.CREATED);
    }
    @GetMapping(EndPoints.INFO)
    public ResponseEntity<WorkDayShiftPlanningExecutionDto> getWorkDayShiftPlanningExecution(@PathVariable Long id)
    {
        WorkDayShiftPlanningExecutionDto foundWorkDayShiftPlanningExecution = workDayShiftPlanningExecutionService.checkedFindById(id);
        return new ResponseEntity<>(foundWorkDayShiftPlanningExecution, HttpStatus.CREATED);
    }
    @GetMapping
    public List<WorkDayShiftPlanningExecutionDto> listWorkDayShiftPlanningExecution() {
        return workDayShiftPlanningExecutionService.findAll();
    }
}
