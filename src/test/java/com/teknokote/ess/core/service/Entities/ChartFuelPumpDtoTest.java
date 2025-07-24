package com.teknokote.ess.core.service.Entities;

import com.teknokote.ess.dto.charts.ChartFuelPumpDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChartFuelPumpDtoTest {

    @Test
    void testDefaultConstructorAndGetters() {
        // Arrange
        ChartFuelPumpDto dto = new ChartFuelPumpDto();

        // Act
        String nameF = dto.getNameF();
        Long pump = dto.getPump();
        String dateF = dto.getDateF();
        Double sumF = dto.getSumF();

        // Assert
        assertEquals(null, nameF);
        assertEquals(null, pump);
        assertEquals(null, dateF);
        assertEquals(null, sumF);
    }

    @Test
    void testParameterizedConstructorAndGetters() {
        // Arrange
        String nameF = "Premium Diesel";
        Long pump = 101L;
        String dateF = "2023-10-01";
        Double sumF = 1500.50;
        ChartFuelPumpDto dto = new ChartFuelPumpDto(nameF, pump, dateF, sumF);

        // Act & Assert
        assertEquals(nameF, dto.getNameF());
        assertEquals(pump, dto.getPump());
        assertEquals(dateF, dto.getDateF());
        assertEquals(sumF, dto.getSumF());
    }

    @Test
    void testSetters() {
        // Arrange
        ChartFuelPumpDto dto = new ChartFuelPumpDto();
        String expectedNameF = "Regular Petrol";
        Long expectedPump = 202L;
        String expectedDateF = "2023-10-02";
        Double expectedSumF = 2500.75;

        // Act
        dto.setNameF(expectedNameF);
        dto.setPump(expectedPump);
        dto.setDateF(expectedDateF);
        dto.setSumF(expectedSumF);

        // Assert
        assertEquals(expectedNameF, dto.getNameF());
        assertEquals(expectedPump, dto.getPump());
        assertEquals(expectedDateF, dto.getDateF());
        assertEquals(expectedSumF, dto.getSumF());
    }
}