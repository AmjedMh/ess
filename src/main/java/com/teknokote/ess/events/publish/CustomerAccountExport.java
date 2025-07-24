package com.teknokote.ess.events.publish;

import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.events.publish.cm.CMSupplierDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface CustomerAccountExport
{
   void customerAccountCreated(CustomerAccountDto accountDto);
   void customerAccountUpdated(CustomerAccountDto accountDto);
   void salePointCreated(Long exportedCustomerAccountId,StationDto stationDto);
   void salePointUpdated(Long exportedCustomerAccountId,StationDto stationDto);
   void userCreated(Long exportedCustomerAccountId,UserDto cmUser);
   void userUpdated(Long exportedCustomerAccountId,UserDto cmUser);
   void productCreated(FuelGradeConfigDto fuelGrade);
   Optional<CMSupplierDto> exportedCustomerAccount(String reference);
}
