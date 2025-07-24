package com.teknokote.ess.core.service.impl.validators;

import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.core.dao.StationDao;
import com.teknokote.ess.core.service.impl.ControllerService;
import com.teknokote.ess.core.service.impl.CountryService;
import com.teknokote.ess.dto.ControllerPtsDto;
import com.teknokote.ess.dto.PumpAttendantDto;
import com.teknokote.ess.dto.StationDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@AllArgsConstructor
public class StationValidator implements ESSValidator<StationDto>
{
   public static final String ERROR_EXISTING_CONTROLLER_WITH_SAME_PTS_ID = "Un contrôleur avec le même PTS ID existe déjà.";
   public static final String ERROR_STATION_NOT_FOUND="station inexistante";
   public static final String ERROR_EXISTING_STATION="station existe déja";
   public static final String ERROR_STATION_MUST_HAVE_COUNTRY = "La station doit avoir un pays.";
   public static final String ERROR_STATION_MUST_HAVE_CONTROLLER = "La station doit avoir un contrôleur.";
   @Autowired
   private ControllerService controllerService;
   @Autowired
   private CountryService countryService;
   @Autowired
   private  PumpAttendantValidator pumpAttendantValidator;
   @Autowired
   private StationDao stationDao;
   @Override
   public ESSValidationResult validateOnCreate(StationDto stationDto)
   {
      ESSValidationResult validationResult=structuralValidation(stationDto);
      // Doit avoir un contrôleur
      if(Objects.isNull(stationDto.getControllerPts())){
         validationResult.foundConsistencyError(ERROR_STATION_MUST_HAVE_CONTROLLER);
      }
      else
      {
         final Optional<StationDto> station = stationDao.findByIdentifier(stationDto.getIdentifier());
         station.ifPresent(stationDto1 -> validationResult.foundConsistencyError(ERROR_EXISTING_STATION));
         final String ptsIdToCreate = stationDto.getControllerPts().getPtsId();
         final Optional<ControllerPtsDto> controller = controllerService.findControllerDto(ptsIdToCreate);
         controller.ifPresent(controllerPtsDto -> validationResult.foundConsistencyError(ERROR_EXISTING_CONTROLLER_WITH_SAME_PTS_ID));
      }
      // Doit avoir un pays existant
      if(Objects.nonNull(stationDto.getCountry()) && countryService.findById(stationDto.getCountry().getId()).isEmpty()){
         validationResult.foundConsistencyError( "Pays inexistant id("+stationDto.getCountry().getId()+")");
      }
      return validationResult;
   }

   public  ESSValidationResult validateOnAddPumpAttendant(PumpAttendantDto pumpAttendantDto){
      return pumpAttendantValidator.validateOnCreate(pumpAttendantDto);
   }

   @Override
   public ESSValidationResult validateOnUpdate(StationDto stationDto)
   {
      return structuralValidation(stationDto);
   }

}
