package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import com.teknokote.ess.events.publish.CustomerAccountExportImpl;
import com.teknokote.ess.events.publish.cm.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerAccountExportImplTest {

    @InjectMocks
    private CustomerAccountExportImpl customerAccountExport;

    @Mock
    private CardManagerClient cardManagerClient;

    @Mock
    private CMSupplierMapper cmSupplierMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCustomerAccountCreated() {
        // Arrange
        CustomerAccountDto accountDto = CustomerAccountDto.builder().build();
        CMSupplierDto supplierDto = CMSupplierDto.builder().build();
        when(cmSupplierMapper.customerAccountToSupplier(accountDto)).thenReturn(supplierDto);

        // Act
        customerAccountExport.customerAccountCreated(accountDto);

        // Assert
        verify(cardManagerClient).createSupplier(supplierDto);
    }

    @Test
    void testCustomerAccountUpdated() {
        // Arrange
        CustomerAccountDto accountDto = CustomerAccountDto.builder().build();
        CMSupplierDto supplierDto = CMSupplierDto.builder().build();
        when(cmSupplierMapper.customerAccountToSupplier(accountDto)).thenReturn(supplierDto);

        // Act
        customerAccountExport.customerAccountUpdated(accountDto);

        // Assert
        verify(cardManagerClient).updateSupplier(supplierDto);
    }

    @Test
    void testSalePointCreated() {
        // Arrange
        Long exportedCustomerAccountId = 1L;
        StationDto stationDto = StationDto.builder().build();
        CMSalePointDto salePointDto = CMSalePointDto.builder().build();
        when(cmSupplierMapper.mapStationToSalesPoint(stationDto)).thenReturn(salePointDto);

        // Act
        customerAccountExport.salePointCreated(exportedCustomerAccountId, stationDto);

        // Assert
        verify(cardManagerClient).createSalePoint(exportedCustomerAccountId, salePointDto);
    }

    @Test
    void testSalePointUpdated() {
        // Arrange
        Long exportedCustomerAccountId = 1L;
        StationDto stationDto = StationDto.builder().build();
        CMSalePointDto salePointDto = CMSalePointDto.builder().build();
        when(cmSupplierMapper.mapStationToSalesPoint(stationDto)).thenReturn(salePointDto);

        // Act
        customerAccountExport.salePointUpdated(exportedCustomerAccountId, stationDto);

        // Assert
        verify(cardManagerClient).updateSalePoint(exportedCustomerAccountId, salePointDto);
    }

    @Test
    void testUserCreated() {
        // Arrange
        Long exportedCustomerAccountId = 1L;
        UserDto userDto = UserDto.builder().build();
        CMUser cmUser = CMUser.builder().build();
        when(cmSupplierMapper.mapUserToCMUser(userDto)).thenReturn(cmUser);

        // Act
        customerAccountExport.userCreated(exportedCustomerAccountId, userDto);

        // Assert
        verify(cardManagerClient).createUser(exportedCustomerAccountId, cmUser);
    }

    @Test
    void testUserUpdated() {
        // Arrange
        Long exportedCustomerAccountId = 1L;
        UserDto userDto = UserDto.builder().build();
        CMUser cmUser = CMUser.builder().build();
        when(cmSupplierMapper.mapUserToCMUser(userDto)).thenReturn(cmUser);

        // Act
        customerAccountExport.userUpdated(exportedCustomerAccountId, userDto);

        // Assert
        verify(cardManagerClient).updateCMUser(exportedCustomerAccountId, cmUser);
    }

    @Test
    void testProductCreated() {
        // Arrange
        FuelGradeConfigDto fuelGradeDto = new FuelGradeConfigDto();
        CMProduct cmProduct = CMProduct.builder().build();
        when(cmSupplierMapper.mapFuelGradeToCMProduct(fuelGradeDto)).thenReturn(cmProduct);

        // Act
        customerAccountExport.productCreated(fuelGradeDto);

        // Assert
        verify(cardManagerClient).createProduct(cmProduct);
    }

    @Test
    void testExportedCustomerAccount() {
        // Arrange
        String reference = "SomeReference";
        CMSupplierDto expectedSupplierDto = CMSupplierDto.builder().build();
        when(cardManagerClient.getSupplier(reference)).thenReturn(Optional.of(expectedSupplierDto));

        // Act
        Optional<CMSupplierDto> result = customerAccountExport.exportedCustomerAccount(reference);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedSupplierDto, result.get());
        verify(cardManagerClient).getSupplier(reference);
    }

    @Test
    void testExportedCustomerAccountNotFound() {
        // Arrange
        String reference = "NonExistentReference";
        when(cardManagerClient.getSupplier(reference)).thenReturn(Optional.empty());

        // Act
        Optional<CMSupplierDto> result = customerAccountExport.exportedCustomerAccount(reference);

        // Assert
        assertFalse(result.isPresent());
        verify(cardManagerClient).getSupplier(reference);
    }
}