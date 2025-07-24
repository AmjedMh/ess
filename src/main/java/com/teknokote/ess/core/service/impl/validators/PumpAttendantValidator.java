package com.teknokote.ess.core.service.impl.validators;

import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.core.dao.impl.PumpAttendantDaoImpl;
import com.teknokote.ess.dto.PumpAttendantDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PumpAttendantValidator implements ESSValidator<PumpAttendantDto>
{
    public static final String USER_WITH_SAME_USERNAME_ALREADY_EXIST = "A user already exists!";
    @Autowired
    private PumpAttendantDaoImpl pumpAttendantDao;
    @Override
    public ESSValidationResult validateOnCreate(PumpAttendantDto dto) {
        return structuralValidation(dto);
    }
}
