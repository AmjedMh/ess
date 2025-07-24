package com.teknokote.ess.core.service.impl.validators;

import com.teknokote.ess.core.dao.UserDao;
import com.teknokote.core.service.ESSValidationError;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.dto.UserDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class UserValidator implements ESSValidator<UserDto>
{
    public static final String USER_WITH_SAME_USERNAME_ALREADY_EXIST = "A user with same user name already exists!";
    @Autowired
    private UserDao userDao;
    @Override
    public ESSValidationResult validateOnCreate(UserDto dto) {
        ESSValidationResult validationResult=structuralValidation(dto);
        if(validationResult.hasErrors()) return validationResult;
        final Optional<UserDto> user = userDao.findByIdentifier(dto.getUsername());
        user.ifPresent(controllerPtsDto -> validationResult.foundError(ESSValidationError.EnumErrorType.CONSISTENCY, USER_WITH_SAME_USERNAME_ALREADY_EXIST));
        return validationResult;
    }

    @Override
    public ESSValidationResult validateOnUpdate(UserDto dto) {
        ESSValidationResult validationResult=structuralValidation(dto);
        if(validationResult.hasErrors()) return validationResult;
        return validationResult;
    }
    @Override
    public ESSValidationResult structuralValidation(UserDto userDto)
    {
        ESSValidationResult validationResult=ESSValidationResult.initEmpty();
        try(final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory())
        {
            final Set<ConstraintViolation<UserDto>> constraintViolations = validatorFactory.getValidator().validate(userDto);
            constraintViolations.stream().map(el -> el.getPropertyPath() + ":" + el.getMessage()).forEach(validationResult::foundFormatError);
        }
        return validationResult;
    }
}


