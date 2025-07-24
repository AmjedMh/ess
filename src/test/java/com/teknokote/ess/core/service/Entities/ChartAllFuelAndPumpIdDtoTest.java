package com.teknokote.ess.core.service.Entities;

import com.teknokote.ess.dto.charts.ChartAllFuelAndPumpIdDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChartAllFuelAndPumpIdDtoTest {

    @Test
    void testDefaultConstructorAndGetters() {
        // Arrange
        ChartAllFuelAndPumpIdDto dto = new ChartAllFuelAndPumpIdDto();

        // Act
        Long pump = dto.getPump();
        String date = dto.getDate();
        Double sum = dto.getSum();

        // Assert
        assertEquals(null, pump);
        assertEquals(null, date);
        assertEquals(null, sum);
    }

    @Test
    void testParameterizedConstructorAndGetters() {
        // Arrange
        Long pump = 1L;
        String date = "2023-10-01";
        Double sum = 100.0;
        ChartAllFuelAndPumpIdDto dto = new ChartAllFuelAndPumpIdDto(pump, date, sum);

        // Act & Assert
        assertEquals(pump, dto.getPump());
        assertEquals(date, dto.getDate());
        assertEquals(sum, dto.getSum());
    }

    @Test
    void testSetters() {
        // Arrange
        ChartAllFuelAndPumpIdDto dto = new ChartAllFuelAndPumpIdDto();
        Long expectedPump = 2L;
        String expectedDate = "2023-10-02";
        Double expectedSum = 200.0;

        // Act
        dto.setPump(expectedPump);
        dto.setDate(expectedDate);
        dto.setSum(expectedSum);

        // Assert
        assertEquals(expectedPump, dto.getPump());
        assertEquals(expectedDate, dto.getDate());
        assertEquals(expectedSum, dto.getSum());
    }
}
