package com.teknokote.ess.controller.front.organization;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.organization.PumpAttendantTeamService;
import com.teknokote.ess.dto.organization.DynamicPumpAttendantTeamDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.PUMP_ATTENDANT_TEAM_ROOT)
public class PumpAttendantTeamController {
    @Autowired
    private PumpAttendantTeamService pumpAttendantTeamService;

    @PostMapping(EndPoints.ADD)
    public ResponseEntity<PumpAttendantTeamDto> addPumpAttendantTeam(@PathVariable Long stationId, @RequestBody PumpAttendantTeamDto dto, HttpServletRequest request) {
        PumpAttendantTeamDto savedPumpAttendantTeam = pumpAttendantTeamService.addTeam(stationId, dto,request);
        return new ResponseEntity<>(savedPumpAttendantTeam, HttpStatus.CREATED);
    }

    @PutMapping(EndPoints.UPDATE)
    public ResponseEntity<PumpAttendantTeamDto> updatePumpAttendantTeam(@PathVariable Long stationId, @RequestBody PumpAttendantTeamDto dto,HttpServletRequest request) {
        PumpAttendantTeamDto savedPumpAttendantTeam = pumpAttendantTeamService.updateTeam(stationId, dto,request);
        return new ResponseEntity<>(savedPumpAttendantTeam, HttpStatus.CREATED);
    }

    @GetMapping(EndPoints.INFO)
    public ResponseEntity<PumpAttendantTeamDto> getPumpAttendantTeam(@PathVariable String stationId, @PathVariable Long id) {
        PumpAttendantTeamDto foundPumpAttendantTeam = pumpAttendantTeamService.checkedFindById(id);
        return new ResponseEntity<>(foundPumpAttendantTeam, HttpStatus.CREATED);
    }

    @GetMapping
    public List<DynamicPumpAttendantTeamDto> listPumpAttendantTeam(@PathVariable Long stationId) {
        return pumpAttendantTeamService.getDynamicPumpAttendantTeam(stationId);
    }

    @GetMapping(EndPoints.LIST_OF_TEAM_BY_ROTATION)
    public List<DynamicPumpAttendantTeamDto> listPumpAttendantTeamByRotation(@PathVariable Long stationId, @PathVariable Long shiftRotationId) {
        return pumpAttendantTeamService.getDynamicPumpAttendantTeams(stationId, shiftRotationId);
    }
}
