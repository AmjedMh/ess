package com.teknokote.ess.core.service.impl.validators;

import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.dto.ControllerPtsDto;
import org.springframework.stereotype.Component;

@Component
public class ControllerValidator implements ESSValidator<ControllerPtsDto>
{
   @Override
   public ESSValidationResult validateOnCreate(ControllerPtsDto dto)
   {
      return ESSValidationResult.initEmpty();
   }
}
