package com.teknokote.ess.controller.front.shifts;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.shifts.AffectedPumpAttendantService;
import com.teknokote.ess.dto.shifts.AffectedPumpAttendantDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.AFFECTED_PUMP_ATTENDANT_ROOT)
public class AffectedPumpAttendantController
{
   @Autowired
   private AffectedPumpAttendantService affectedPumpAttendantService;


   @PostMapping(EndPoints.ADD)
   public ResponseEntity<AffectedPumpAttendantDto> addAffectedPumpAttendant(@RequestBody AffectedPumpAttendantDto dto)
   {
      AffectedPumpAttendantDto savedAffectedPumpAttendant = affectedPumpAttendantService.create(dto);
      return new ResponseEntity<>(savedAffectedPumpAttendant, HttpStatus.CREATED);
   }


   @PutMapping(EndPoints.UPDATE)
   public ResponseEntity<AffectedPumpAttendantDto> updateAffectedPumpAttendant(@RequestBody AffectedPumpAttendantDto dto)
   {
      AffectedPumpAttendantDto savedAffectedPumpAttendant = affectedPumpAttendantService.update(dto);
      return new ResponseEntity<>(savedAffectedPumpAttendant, HttpStatus.CREATED);
   }

   @GetMapping(EndPoints.INFO)
   public ResponseEntity<AffectedPumpAttendantDto> getAffectedPumpAttendant(@PathVariable Long id)
   {
      AffectedPumpAttendantDto foundAffectedPumpAttendant = affectedPumpAttendantService.checkedFindById(id);
      return new ResponseEntity<>(foundAffectedPumpAttendant, HttpStatus.CREATED);
   }

   @GetMapping
   public List<AffectedPumpAttendantDto> listAffectedPumpAttendant()
   {
      return affectedPumpAttendantService.findAll();
   }
}
