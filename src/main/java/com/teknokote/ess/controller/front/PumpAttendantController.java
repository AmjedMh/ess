package com.teknokote.ess.controller.front;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.PumpAttendantService;
import com.teknokote.ess.core.service.impl.PumpAttendantServiceImpl;
import com.teknokote.ess.dto.PumpAttendantDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.PUMP_ATTENDANT_ROOT)
public class PumpAttendantController {
    @Autowired
    private PumpAttendantService pumpAttendantService;
    @Autowired
    private PumpAttendantServiceImpl pumpAttendantServiceImpl;


    @PostMapping(EndPoints.ADD)
    public ResponseEntity<PumpAttendantDto> addPumpAttendant(@PathVariable Long stationId,
                                                             @RequestPart(name = "dto") PumpAttendantDto dto,
                                                             @RequestParam(name = "photoFile", required = false) MultipartFile photoFile, HttpServletRequest request) {
        PumpAttendantDto savedPumpAttendant = pumpAttendantServiceImpl.addPumpAttendant(stationId, dto, photoFile,request);
        return new ResponseEntity<>(savedPumpAttendant, HttpStatus.CREATED);
    }


    @PostMapping(EndPoints.UPDATE)
    public ResponseEntity<PumpAttendantDto> updatePumpAttendant(@PathVariable Long stationId,
                                                                @RequestPart(name = "dto") PumpAttendantDto dto,
                                                                @RequestParam(name = "photoFile", required = false) MultipartFile photoFile,HttpServletRequest request) {
        PumpAttendantDto updatedPumpAttendant = pumpAttendantServiceImpl.updatePumpAttendant(stationId, dto, photoFile,request);
        return new ResponseEntity<>(updatedPumpAttendant, HttpStatus.OK);
    }


    @GetMapping(EndPoints.IMAGES)
    public String serveImage(@PathVariable String imageName) throws IOException {
        return pumpAttendantServiceImpl.serveImageBase64(imageName);
    }

    @GetMapping()
    public Page<PumpAttendantDto> listPumpAttendant(@PathVariable Long stationId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "50") int size) {

        return pumpAttendantServiceImpl.findAllByStation(stationId, page, size);
    }

    @GetMapping(EndPoints.LIST_BY_ACTIF)
    public List<PumpAttendantDto> listPumpAttendantByActif(@PathVariable Long stationId,
                                                           @PathVariable boolean actif,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return pumpAttendantServiceImpl.findByStation(stationId, actif, page, size);
    }

    @PostMapping(EndPoints.DEACTIVATE)
    public ResponseEntity<PumpAttendantDto> deactivatePumpAttendant(@PathVariable Long stationId,@PathVariable Long id,HttpServletRequest request) {
        return ResponseEntity.ok(pumpAttendantService.deactivatePumpAttendant(stationId,id,request));
    }

    @PostMapping(EndPoints.ACTIVATE)
    public ResponseEntity<PumpAttendantDto> activatePumpAttendant(@PathVariable Long stationId,@PathVariable Long id,HttpServletRequest request) {
        return ResponseEntity.ok(pumpAttendantService.activatePumpAttendant(stationId,id,request));
    }

    @GetMapping(EndPoints.GET_BY_TAG)
    public ResponseEntity<PumpAttendantDto> getByTag(@PathVariable Long stationId, @PathVariable String tag) {
        return ResponseEntity.ok(pumpAttendantService.findByStationAndTag(stationId, tag));
    }

    @GetMapping(EndPoints.GET_BY_MATRICULE)
    public ResponseEntity<PumpAttendantDto> getByMatricule(@PathVariable Long stationId, @PathVariable String matricule) {
        return ResponseEntity.ok(pumpAttendantService.findByStationAndMatricule(stationId, matricule));
    }

    @GetMapping(EndPoints.PUMP_ATTENDANT_INFO)
    public ResponseEntity<PumpAttendantDto> getPumpAttendantDetails(@PathVariable Long stationId, @PathVariable Long id) {
        return ResponseEntity.ok(pumpAttendantService.findByIdAndStationId(id, stationId));
    }

}
