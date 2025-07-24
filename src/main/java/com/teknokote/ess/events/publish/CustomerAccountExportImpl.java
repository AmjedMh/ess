package com.teknokote.ess.events.publish;

import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.events.publish.cm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class CustomerAccountExportImpl implements CustomerAccountExport
{
   @Autowired
   private CardManagerClient cardManagerClient;
   @Autowired
   private CMSupplierMapper cmSupplierMapper;
   @Override
   public void customerAccountCreated(CustomerAccountDto accountDto)
   {
      CMSupplierDto supplierDto = cmSupplierMapper.customerAccountToSupplier(accountDto);
      cardManagerClient.createSupplier(supplierDto);
   }

   @Override
   public void customerAccountUpdated(CustomerAccountDto accountDto) {
      CMSupplierDto supplierDto = cmSupplierMapper.customerAccountToSupplier(accountDto);
      cardManagerClient.updateSupplier(supplierDto);
   }

   @Override
   public void salePointCreated(Long exportedCustomerAccountId,StationDto stationDto) {
      CMSalePointDto salePointDto = cmSupplierMapper.mapStationToSalesPoint(stationDto);
      cardManagerClient.createSalePoint(exportedCustomerAccountId,salePointDto);
   }

   @Override
   public void salePointUpdated(Long exportedCustomerAccountId,StationDto stationDto) {
      CMSalePointDto salePointDto = cmSupplierMapper.mapStationToSalesPoint(stationDto);
      cardManagerClient.updateSalePoint(exportedCustomerAccountId,salePointDto);
   }

   @Override
   public void userCreated(Long exportedCustomerAccountId,UserDto user) {
      CMUser cmUser = cmSupplierMapper.mapUserToCMUser(user);
      cardManagerClient.createUser(exportedCustomerAccountId,cmUser);
   }

   @Override
   public void userUpdated(Long exportedCustomerAccountId,UserDto user) {
      CMUser cmUser = cmSupplierMapper.mapUserToCMUser(user);
      cardManagerClient.updateCMUser(exportedCustomerAccountId,cmUser);
   }

   @Override
   public void productCreated(FuelGradeConfigDto fuelGrade) {
      CMProduct cmProduct = cmSupplierMapper.mapFuelGradeToCMProduct(fuelGrade);
      cardManagerClient.createProduct(cmProduct);
   }

   @Override
   public Optional<CMSupplierDto> exportedCustomerAccount(String reference) {
      return cardManagerClient.getSupplier(reference);
   }
}
