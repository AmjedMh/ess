package com.teknokote.ess.core.service.impl.validators.shifts;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.core.dao.shifts.ShiftRotationDao;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShiftRotationValidator implements ESSValidator<ShiftRotationDto>
{
    @Override
    public ESSValidationResult validate(ShiftRotationDto dto)
    {
        final ESSValidationResult results = ESSValidator.super.validate(dto);

        if (dto.getStartValidityDate().isAfter(dto.getEndValidityDate())){
            throw new ServiceValidationException("Start Validity Date cannot be after End Validity Date");
        }
        if (!dto.getStartValidityDate().isEqual(dto.getStartValidityDate().withDayOfMonth(1))) {
            throw new ServiceValidationException("Start Validity Date must be the first day of the month");
        }
        if (!dto.getEndValidityDate().isEqual(dto.getEndValidityDate().withDayOfMonth(dto.getEndValidityDate().lengthOfMonth()))) {
            throw new ServiceValidationException("End Validity Date must be the last day of the month");
        }
        return results;
    }

    @Override
    public ESSValidationResult validateOnUpdate(ShiftRotationDto dto)
    {
        return this.validate(dto);
    }

}
