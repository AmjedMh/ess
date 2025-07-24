package com.teknokote.ess.core.service.impl.validators;

import com.teknokote.ess.core.dao.CustomerAccountDao;
import com.teknokote.core.service.ESSValidationError;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.core.service.impl.UserService;
import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class CustomerAccountValidator implements ESSValidator<CustomerAccountDto>
{
   public static final String ERROR_EXISTING_MASTER_USER_WITH_SAME_USERNAME = "l'identifiant du master user existe déjà.";
   public static final String ERROR_EXISTING_CUSTOMER_ACCOUNT_WITH_SAME_NAME = "ERROR_CUSTOMER_ACCOUNT_WITH_SAME_NAME";
   public static final String INEXISTANT_CUSTOMER_ACCOUNT = "INEXISTANT_CUSTOMER_ACCOUNT";
   public static final String STATION_WITH_SAME_NAME_EXISTS_FOR_CUSTOMER_ACCOUNT = "Station déjà existante pour le compte client";
   @Autowired
   private UserService userService;
   @Autowired
   private CustomerAccountDao customerAccountDao;
   @Autowired
   private  StationValidator stationValidator;
   @Autowired
   private  UserValidator userValidator;

   public  ESSValidationResult validateOnAddStation(StationDto stationDto){
      final Optional<CustomerAccountDto> optionalCustomerAccountDto = customerAccountDao.findById(stationDto.getCustomerAccountId());
      if (optionalCustomerAccountDto.isEmpty()) {
         return ESSValidationResult.initError(ESSValidationError.EnumErrorType.CONSISTENCY, INEXISTANT_CUSTOMER_ACCOUNT);
      }
      final ESSValidationResult validationResult = stationValidator.validateOnCreate(stationDto);

      final Set<StationDto> stations = optionalCustomerAccountDto.get().getStations();
      if(stations.contains(stationDto)){
         validationResult.foundError(ESSValidationError.EnumErrorType.CONSISTENCY, STATION_WITH_SAME_NAME_EXISTS_FOR_CUSTOMER_ACCOUNT);
      }
      return validationResult;
   }
   public  ESSValidationResult validateOnAddUser(Long customerAccountId, UserDto userDto){
      final Optional<CustomerAccountDto> optionalCustomerAccountDto = customerAccountDao.findById(customerAccountId);
      if (optionalCustomerAccountDto.isEmpty()) {
         return ESSValidationResult.initError(ESSValidationError.EnumErrorType.CONSISTENCY, INEXISTANT_CUSTOMER_ACCOUNT);
      }
      final ESSValidationResult validationResult = userValidator.validateOnCreate(userDto);

      final Set<UserDto> attachedUsers = optionalCustomerAccountDto.get().getAttachedUsers();
      if(attachedUsers.contains(userDto)){
         validationResult.foundError(ESSValidationError.EnumErrorType.CONSISTENCY, STATION_WITH_SAME_NAME_EXISTS_FOR_CUSTOMER_ACCOUNT);
      }
      return validationResult;
   }
   @Override
   public ESSValidationResult validateOnCreate(CustomerAccountDto customerAccountDto)
   {
      ESSValidationResult validationResult=structuralValidation(customerAccountDto);
      if(validationResult.hasErrors()) return validationResult;
      final String customerAccountIdentifier = customerAccountDto.getIdentifier();
      final Optional<CustomerAccountDto> customerAccount = customerAccountDao.findByIdentifier(customerAccountIdentifier);
      customerAccount.ifPresent(customerDto -> validationResult.foundError(ESSValidationError.EnumErrorType.CONSISTENCY, ERROR_EXISTING_CUSTOMER_ACCOUNT_WITH_SAME_NAME));
      final String masterUserName = customerAccountDto.getMasterUser().getUsername();
      final Optional<UserDto> user = userService.findByIdentifier(masterUserName);
      user.ifPresent(userDto -> validationResult.foundError(ESSValidationError.EnumErrorType.CONSISTENCY, ERROR_EXISTING_MASTER_USER_WITH_SAME_USERNAME));
      return validationResult;
   }

   @Override
   public ESSValidationResult validateOnUpdate(CustomerAccountDto dto)
   {
      return ESSValidator.super.validateOnUpdate(dto);
   }
}

