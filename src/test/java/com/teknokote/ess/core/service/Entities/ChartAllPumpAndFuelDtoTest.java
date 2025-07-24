package com.teknokote.ess.core.service.Entities;

import com.teknokote.ess.dto.charts.ChartAllPumpAndFuelDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChartAllPumpAndFuelDtoTest {

    @Test
    void testDefaultConstructorAndGetters() {
        // Arrange
        ChartAllPumpAndFuelDto dto = new ChartAllPumpAndFuelDto();

        // Act
        String date = dto.getDate();
        double sum = dto.getSum();
        String fuel = dto.getFuel();

        // Assert
        assertEquals(null, date);
        assertEquals(0.0, sum);
        assertEquals(null, fuel);
    }

    @Test
    void testParameterizedConstructorAndGetters() {
        // Arrange
        String date = "2023-10-01";
        double sum = 150.75;
        String fuel = "Diesel";
        ChartAllPumpAndFuelDto dto = new ChartAllPumpAndFuelDto(date, sum, fuel);

        // Act & Assert
        assertEquals(date, dto.getDate());
        assertEquals(sum, dto.getSum());
        assertEquals(fuel, dto.getFuel());
    }

    @Test
    void testSetters() {
        // Arrange
        ChartAllPumpAndFuelDto dto = new ChartAllPumpAndFuelDto();
        String expectedDate = "2023-10-02";
        double expectedSum = 250.50;
        String expectedFuel = "Petrol";

        // Act
        dto.setDate(expectedDate);
        dto.setSum(expectedSum);
        dto.setFuel(expectedFuel);

        // Assert
        assertEquals(expectedDate, dto.getDate());
        assertEquals(expectedSum, dto.getSum());
        assertEquals(expectedFuel, dto.getFuel());
    }
}