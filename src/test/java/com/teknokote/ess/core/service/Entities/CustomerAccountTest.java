package com.teknokote.ess.core.service.Entities;

import com.teknokote.ess.core.model.organization.CustomerAccount;
import com.teknokote.ess.core.model.organization.EnumCustomerAccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CustomerAccountTest {

    private CustomerAccount customerAccount;

    @BeforeEach
    void setUp() {
        customerAccount = new CustomerAccount();
        customerAccount.setName("Test Account");
        customerAccount.setIdentifier("T123");
        customerAccount.setStatus(EnumCustomerAccountStatus.ACCESS_DISABLED);
        customerAccount.setCity("Test City");
        customerAccount.setPhone("1234567890");
        customerAccount.setResaleRight(true);
        customerAccount.setCardManager(false);
        customerAccount.setExported(false);
        customerAccount.setPlannedExportDate(LocalDateTime.now().plusDays(5));
        customerAccount.setScheduledDate(LocalDateTime.now().plusDays(10));
    }

    @Test
    void testCustomerAccountCreation() {
        // Arrange
        String accountName = "New Customer Account";

        // Act
        CustomerAccount createdAccount = CustomerAccount.create(accountName);

        // Assert
        assertNotNull(createdAccount);
        assertEquals(accountName, createdAccount.getName());
        assertEquals(EnumCustomerAccountStatus.ACCESS_DISABLED, createdAccount.getStatus());
    }

    @Test
    void testEqualsSameObject() {
        // Act & Assert
        assertEquals(customerAccount, customerAccount);
    }

    @Test
    void testEqualsDifferentObjectSameName() {
        // Arrange
        CustomerAccount anotherAccount = new CustomerAccount();
        anotherAccount.setName("Test Account");

        // Act & Assert
        assertEquals(customerAccount, anotherAccount);
    }

    @Test
    void testEqualsDifferentName() {
        // Arrange
        CustomerAccount anotherAccount = new CustomerAccount();
        anotherAccount.setName("Different Account");

        // Act & Assert
        assertNotEquals(customerAccount, anotherAccount);
    }

    @Test
    void testHashCodeSameName() {
        // Arrange
        CustomerAccount anotherAccount = new CustomerAccount();
        anotherAccount.setName("Test Account");

        // Act & Assert
        assertEquals(customerAccount.hashCode(), anotherAccount.hashCode());
    }

    @Test
    void testHashCodeDifferentName() {
        // Arrange
        CustomerAccount anotherAccount = new CustomerAccount();
        anotherAccount.setName("Different Account");

        // Act & Assert
        assertNotEquals(customerAccount.hashCode(), anotherAccount.hashCode());
    }

    @Test
    void testGettersAndSetters() {
        assertEquals("Test Account", customerAccount.getName());
        assertEquals("T123", customerAccount.getIdentifier());
        assertEquals(EnumCustomerAccountStatus.ACCESS_DISABLED, customerAccount.getStatus());
        assertEquals("Test City", customerAccount.getCity());
        assertEquals("1234567890", customerAccount.getPhone());
        assertTrue(customerAccount.isResaleRight());
        assertFalse(customerAccount.isCardManager());
        assertFalse(customerAccount.isExported());
        assertNotNull(customerAccount.getPlannedExportDate());
        assertNotNull(customerAccount.getScheduledDate());
    }
}