package com.teknokote.ess.controller.front.shifts;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.shifts.ShiftRotationService;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.teknokote.ess.dto.PeriodDto;

import java.time.LocalDate;
import java.util.List;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.SHIFT_ROTATION_ROOT)
public class ShiftRotationController {
    @Autowired
    private ShiftRotationService shiftRotationService;


    @PostMapping(EndPoints.ADD)
    public ResponseEntity<ShiftRotationDto> addShiftRotation(@PathVariable Long stationId, @RequestBody ShiftRotationDto dto, HttpServletRequest request) {
        dto.setStationId(stationId);
        ShiftRotationDto savedShiftRotation = shiftRotationService.addShiftRotation(stationId, dto,request);
        return new ResponseEntity<>(savedShiftRotation, HttpStatus.CREATED);
    }


    @PutMapping(EndPoints.UPDATE)
    public ResponseEntity<ShiftRotationDto> updateShiftRotation(@PathVariable Long stationId,@RequestBody ShiftRotationDto dto,HttpServletRequest request) {
        dto.setStationId(stationId);
        ShiftRotationDto savedShiftRotation = shiftRotationService.update(dto);
        return new ResponseEntity<>(savedShiftRotation, HttpStatus.CREATED);
    }

    @GetMapping(EndPoints.INFO)
    public ResponseEntity<ShiftRotationDto> getShiftRotation(@PathVariable Long id) {
        ShiftRotationDto foundShiftRotation = shiftRotationService.checkedFindById(id);
        return new ResponseEntity<>(foundShiftRotation, HttpStatus.CREATED);
    }

    @GetMapping
    public List<ShiftRotationDto> listShiftRotation(@PathVariable Long stationId) {
        return shiftRotationService.findAllByStation(stationId);
    }

    @GetMapping(EndPoints.VALID)
    public ShiftRotationDto getValidShiftRotation(@PathVariable Long stationId, @RequestParam LocalDate month) {
        return shiftRotationService.findValidRotation(stationId, month);
    }

    @GetMapping(EndPoints.PERIOD_BY_ROTATION)
    public List<PeriodDto> getPeriodsByRotation(@PathVariable Long stationId) {
        return shiftRotationService.listPeriodTypesForStation(stationId);
    }

    @GetMapping(EndPoints.DELETE_ROTATION)
    public void delete (@PathVariable Long stationId,@PathVariable Long shiftRotationId,HttpServletRequest request){
        shiftRotationService.deleteShiftRotation(stationId,shiftRotationId,request);
    }
}
