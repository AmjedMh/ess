package com.teknokote.ess.controller.front.shifts;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.shifts.ShiftPlanningExecutionDetailService;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDetailDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.SHIFT_PLANNING_EXECUTION_DETAIL_ROOT)
public class ShiftPlanningExecutionDetailController {
    @Autowired
    private ShiftPlanningExecutionDetailService shiftPlanningExecutionDetailService;


    @PostMapping(EndPoints.ADD)
    public ResponseEntity<ShiftPlanningExecutionDetailDto> addShiftPlanningExecutionDetail(@RequestBody ShiftPlanningExecutionDetailDto dto) {
        ShiftPlanningExecutionDetailDto savedShiftPlanningExecutionDetail = shiftPlanningExecutionDetailService.create(dto);
        return new ResponseEntity<>(savedShiftPlanningExecutionDetail, HttpStatus.CREATED);
    }

    @GetMapping(EndPoints.INFO)
    public ResponseEntity<ShiftPlanningExecutionDetailDto> getShiftPlanningExecutionDetail(@PathVariable Long id) {
        ShiftPlanningExecutionDetailDto foundShiftPlanningExecutionDetail = shiftPlanningExecutionDetailService.checkedFindById(id);
        return new ResponseEntity<>(foundShiftPlanningExecutionDetail, HttpStatus.CREATED);
    }

    @GetMapping
    public List<ShiftPlanningExecutionDetailDto> listShiftPlanningExecutionDetail() {
        return shiftPlanningExecutionDetailService.findAll();
    }
}
