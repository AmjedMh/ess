package com.teknokote.ess.controller.front.shifts;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.core.service.shifts.PumpAttendantCollectionSheetService;
import com.teknokote.ess.dto.shifts.PumpAttendantCollectionSheetDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping(EndPoints.PUMP_ATTENDANT_COLLECTION_SHEET_ROOT)
public class PumpAttendantCollectionSheetController
{
   @Autowired
   private PumpAttendantCollectionSheetService pumpAttendantCollectionSheetService;


   @PostMapping(EndPoints.ADD)
   public ResponseEntity<PumpAttendantCollectionSheetDto> addPumpAttendantCollectionSheet(@RequestBody PumpAttendantCollectionSheetDto dto)
   {
      PumpAttendantCollectionSheetDto savedPumpAttendantCollectionSheet = pumpAttendantCollectionSheetService.create(dto);
      return new ResponseEntity<>(savedPumpAttendantCollectionSheet, HttpStatus.CREATED);
   }


   @PutMapping(EndPoints.UPDATE)
   public ResponseEntity<PumpAttendantCollectionSheetDto> updatePumpAttendantCollectionSheet(@RequestBody PumpAttendantCollectionSheetDto dto)
   {
      PumpAttendantCollectionSheetDto savedPumpAttendantCollectionSheet = pumpAttendantCollectionSheetService.update(dto);
      return new ResponseEntity<>(savedPumpAttendantCollectionSheet, HttpStatus.CREATED);
   }

   @GetMapping(EndPoints.INFO)
   public ResponseEntity<PumpAttendantCollectionSheetDto> getPumpAttendantCollectionSheet(@PathVariable Long id)
   {
      PumpAttendantCollectionSheetDto foundPumpAttendantCollectionSheet = pumpAttendantCollectionSheetService.checkedFindById(id);
      return new ResponseEntity<>(foundPumpAttendantCollectionSheet, HttpStatus.CREATED);
   }

   @GetMapping
   public List<PumpAttendantCollectionSheetDto> listPumpAttendantCollectionSheet()
   {
      return pumpAttendantCollectionSheetService.findAll();
   }
}
