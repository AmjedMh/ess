package com.teknokote.ess.core.service.controller;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.controller.front.customer_account.CustomerAccountController;
import com.teknokote.ess.core.service.impl.CustomerAccountService;
import com.teknokote.ess.dto.CustomerAccountDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class CustomerAccountControllerTest {

    @InjectMocks
    private CustomerAccountController customerAccountController;
    @Mock
    private CustomerAccountService customerAccountService;

    private CustomerAccountDto customerAccountDto;
    private Set<StationDto> stations;
    private Set<UserDto> attachedUsers;

    @BeforeEach
    void setUp() {

        // Initialize sets
        stations = new HashSet<>();
        attachedUsers = new HashSet<>();

        // Initialize the CustomerAccountDto with appropriate sample values
        customerAccountDto = CustomerAccountDto.builder()
                .id(1L)
                .name("Sample Account")
                .identifier("ACC-001")
                .city("Sample City")
                .creatorAccountId(1L)
                .creatorCustomerAccountName("Sample Creator")
                .stationsCount(stations.size())
                .stations(stations)
                .attachedUsers(attachedUsers)
                .phone("123-456-7890")
                .build();
    }

    @Test
    void testAddCustomerAccount() {
        // Set up mock behavior to return the predefined DTO
        when(customerAccountService.create(any(CustomerAccountDto.class))).thenReturn(customerAccountDto);

        // Call the controller to add the customer account
        ResponseEntity<Object> response = customerAccountController.addCustomerAccount(customerAccountDto);

        // Assertions to ensure expected behavior
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(customerAccountDto, response.getBody());
        verify(customerAccountService, times(1)).create(customerAccountDto);
    }

    @Test
    void testAddCustomerAccountWithValidationError() {
        // Mock the service to throw a validation exception for this test
        when(customerAccountService.create(any(CustomerAccountDto.class))).thenThrow(new ServiceValidationException("Validation error"));

        ResponseEntity<Object> response = customerAccountController.addCustomerAccount(customerAccountDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation error", response.getBody());
        verify(customerAccountService, times(1)).create(customerAccountDto);
    }
    @Test
    void testUpdateCustomerAccount() {
        // Assuming the updated DTO has the same data for this test
        when(customerAccountService.updateCustomerAccount(any(CustomerAccountDto.class), any())).thenReturn(customerAccountDto);

        ResponseEntity<CustomerAccountDto> response = customerAccountController.updateCustomerAccount(customerAccountDto, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(customerAccountDto, response.getBody());
        verify(customerAccountService, times(1)).updateCustomerAccount(customerAccountDto, null);
    }

}