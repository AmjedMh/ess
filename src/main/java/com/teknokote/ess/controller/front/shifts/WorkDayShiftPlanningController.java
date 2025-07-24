package com.teknokote.ess.controller.front.shifts;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningService;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.WORK_DAY_SHIFT_PLANNING_ROOT)
public class WorkDayShiftPlanningController {
@Autowired
private WorkDayShiftPlanningService workDayShiftPlanningService;


@PostMapping(EndPoints.ADD)
public ResponseEntity<WorkDayShiftPlanningDto> addWorkDayShiftPlanning(@RequestBody WorkDayShiftPlanningDto dto) {
    WorkDayShiftPlanningDto savedWorkDayShiftPlanning = workDayShiftPlanningService.create(dto);
    return new ResponseEntity<>(savedWorkDayShiftPlanning, HttpStatus.CREATED);
    }


    @PutMapping(EndPoints.UPDATE)
    public ResponseEntity<WorkDayShiftPlanningDto> updateWorkDayShiftPlanning(@RequestBody WorkDayShiftPlanningDto dto) {
        WorkDayShiftPlanningDto savedWorkDayShiftPlanning = workDayShiftPlanningService.update(dto);
        return new ResponseEntity<>(savedWorkDayShiftPlanning, HttpStatus.CREATED);
    }
    @GetMapping(EndPoints.INFO)
    public ResponseEntity<WorkDayShiftPlanningDto> getWorkDayShiftPlanning(@PathVariable Long id)
    {
        WorkDayShiftPlanningDto foundWorkDayShiftPlanning = workDayShiftPlanningService.checkedFindById(id);
        return new ResponseEntity<>(foundWorkDayShiftPlanning, HttpStatus.CREATED);
    }
    @GetMapping
    public List<WorkDayShiftPlanningDto> listWorkDayShiftPlanning() {
        return workDayShiftPlanningService.findAll();
    }
}
