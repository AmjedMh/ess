package com.teknokote.ess.core.service.impl.validators.shifts;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSGeneralValidator;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.ess.core.dao.shifts.ShiftPlanningExecutionDao;
import com.teknokote.ess.core.service.impl.StationService;
import com.teknokote.ess.dto.shifts.ShiftDetailUpdatesDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDetailDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ShiftDetailUpdatesValidator implements ESSGeneralValidator<ShiftDetailUpdatesDto>
{
   @Autowired
   private ShiftPlanningExecutionDao shiftPlanningExecutionDao;
   @Autowired
   private StationService stationService;


   @Override
   public ESSValidationResult validate(ShiftDetailUpdatesDto dto)
   {
      final ESSValidationResult results = ESSGeneralValidator.super.validate(dto);
      final Optional<ShiftPlanningExecutionDto> existingShiftExecution = shiftPlanningExecutionDao.findByStation(dto.getShiftPlanningExecutionId(), dto.getStationId());
      if(existingShiftExecution.isEmpty()){
         throw new ServiceValidationException("Unknown shift planning execution!");
      }
      if(existingShiftExecution.get().isLocked()){
         throw new ServiceValidationException("Shift planning execution already locked!");
      }
      final Optional<ShiftPlanningExecutionDetailDto> shiftPlanningExecutionDetailToUpdate = existingShiftExecution.get().getShiftPlanningExecutionDetail()
         .stream().filter(el -> el.getId().equals(dto.getId())).findFirst();

      if(shiftPlanningExecutionDetailToUpdate.isEmpty()){
         throw new ServiceValidationException("Unknown shift planning execution detail!");
      }
      // GSDS-469: RG 2. Positionnement strict de l’heure de début avant l’heure de fin :
      // L’heure de début doit toujours être positionnée de manière stricte avant (inférieure à) l’heure de fin en date ET heure.
      if(dto.getEndDateTime().isBefore(dto.getStartDateTime())){
         throw new ServiceValidationException("End date/time is before start date/time");
      }

      // GSDS-469: RG 3. Contrainte sur l’heure de début/heure de fin pour un pompiste par rapport au poste :
      // L’heure de début ou l’heure de fin pour n’importe quel pompiste ne doit pas être antérieure à l’heure définie pour un poste.
      if(dto.getStartDateTime().isBefore(existingShiftExecution.get().getStartDateTime())){
         throw new ServiceValidationException("PumpAttendant start date/time is before shift start date/time");
      }
      if(dto.getEndDateTime().isBefore(existingShiftExecution.get().getStartDateTime())){
         throw new ServiceValidationException("PumpAttendant end date/time is before shift start date/time");
      }
      if (dto.getEndDateTime().isAfter(existingShiftExecution.get().getEndDateTime())){
         throw new ServiceValidationException("PumpAttendant end date/time is before shift end date/time");
      }
      // GSDS-469: RG 13 Si une transaction a été enregistrée pour le pompiste avant l'heure de début prédéfinie, le système génère un avertissement.
      stationService.checkedFindById(dto.getStationId());
      return results;
   }
}
