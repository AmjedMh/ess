package com.teknokote.ess.controller.front.shifts;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.shifts.ShiftPlanningService;
import com.teknokote.ess.dto.organization.DynamicShiftPlanningDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.SHIFT_PLANNING_ROOT)
public class ShiftPlanningController {
    @Autowired
    private ShiftPlanningService shiftPlanningService;


    @PostMapping(EndPoints.ADD)
    public ResponseEntity<ShiftPlanningDto> addShiftPlanning(@RequestBody ShiftPlanningDto dto) {
        ShiftPlanningDto savedShiftPlanning = shiftPlanningService.create(dto);
        return new ResponseEntity<>(savedShiftPlanning, HttpStatus.CREATED);
    }

    @PostMapping(EndPoints.GENERATE)
    public ResponseEntity<List<ShiftPlanningDto>> generateShiftPlanning(@PathVariable Long stationId,
                                                                        @RequestParam LocalDate month, HttpServletRequest request) {
        shiftPlanningService.deleteShiftPlanning(stationId, month);
        final List<ShiftPlanningDto> generatedList = shiftPlanningService.generatePlanning(stationId, month,request);
        List<ShiftPlanningDto> savedShiftPlanningDtos = shiftPlanningService.createList(generatedList);
        return new ResponseEntity<>(savedShiftPlanningDtos, HttpStatus.CREATED);
    }

    @PostMapping(EndPoints.RESET)
    public ResponseEntity<List<ShiftPlanningDto>> resetShiftPlanning(@PathVariable Long stationId,
                                                                     @RequestParam LocalDate month,HttpServletRequest request) {
        final List<ShiftPlanningDto> regeneratedList = shiftPlanningService.resetShiftPlanning(stationId, month,request);
        return new ResponseEntity<>(regeneratedList, HttpStatus.CREATED);
    }


    @PutMapping(EndPoints.UPDATE)
    public ResponseEntity<ShiftPlanningDto> updateShiftPlanning(@RequestBody ShiftPlanningDto dto) {
        ShiftPlanningDto savedShiftPlanning = shiftPlanningService.update(dto);
        return new ResponseEntity<>(savedShiftPlanning, HttpStatus.CREATED);
    }

    @PostMapping(EndPoints.UPDATE_LIST)
    public ResponseEntity<List<ShiftPlanningDto>> updateShiftPlanningList(@RequestBody List<ShiftPlanningDto> dto) {
        List<ShiftPlanningDto> savedShiftPlanning = shiftPlanningService.updateList(dto);
        return new ResponseEntity<>(savedShiftPlanning, HttpStatus.CREATED);
    }

    @GetMapping(EndPoints.INFO)
    public ResponseEntity<ShiftPlanningDto> getShiftPlanning(@PathVariable Long id) {
        ShiftPlanningDto foundShiftPlanning = shiftPlanningService.checkedFindById(id);
        return new ResponseEntity<>(foundShiftPlanning, HttpStatus.CREATED);
    }

    @GetMapping(EndPoints.LIST_OF_TEAM)
    public ResponseEntity<List<PumpAttendantTeamDto>> listTeamsByRotation(@PathVariable Long stationId, @PathVariable Long shiftRotationId) {
        List<PumpAttendantTeamDto> teamDtoList = shiftPlanningService.findTeamsByStationAndShiftRotation(stationId, shiftRotationId);
        return new ResponseEntity<>(teamDtoList, HttpStatus.CREATED);
    }

    @GetMapping
    public List<DynamicShiftPlanningDto> listShiftPlanning(@PathVariable Long stationId, @RequestParam LocalDate month) {
        final List<ShiftPlanningDto> shiftPlannings = shiftPlanningService.findByStationAndMonth(stationId, month);
        if (CollectionUtils.isEmpty(shiftPlannings)) {
            return List.of();
        }
        return shiftPlannings.stream().map(shiftPlanningService::mapToDynamicDto).sorted(Comparator
                        .comparing(DynamicShiftPlanningDto::getId))
                .toList();
    }
}
