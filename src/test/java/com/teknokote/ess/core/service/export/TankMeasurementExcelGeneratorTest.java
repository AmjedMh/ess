package com.teknokote.ess.core.service.export;

import com.teknokote.ess.core.service.impl.tank.TankMeasurementExcelGenerator;
import com.teknokote.ess.dto.TankMeasurementsDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class TankMeasurementExcelGeneratorTest {

    @Test
    void testGenerateMeasurementExcel() throws IOException {
        // Arrange
        List<TankMeasurementsDto> tankMeasurements = Arrays.asList(
                createTankMeasurementsDto(LocalDateTime.now(), "No alarms", "1", 100.0, 200.0,
                        50.0, 100.0, 800.0, 300.0, 25.5, 80.0),
                createTankMeasurementsDto(LocalDateTime.now().plusHours(1), "Alarm1", "2", 90.0, 150.0,
                        45.0, 90.0, 600.0, 280.0, 24.5, 70.0)
        );
        List<String> columnsToDisplay = Arrays.asList("dateTime", "alarms", "tank", "productHeight","productVolume",
                "waterHeight", "waterVolume", "productDensity", "productMass", "tankFillingPercentage", "temperature");
        String locale = Locale.ENGLISH.toString();
        String filterSummary = "Test Summary";

        // Act
        byte[] excelData = TankMeasurementExcelGenerator.generateMeasurementExcel(tankMeasurements, columnsToDisplay, locale, filterSummary);

        // Assert
        Assertions.assertNotNull(excelData);
        Assertions.assertTrue(excelData.length > 0);
    }

    @Test
    void testFormatPorcentage() {
        // Test normal case
        String formatted = TankMeasurementExcelGenerator.formatPorcentage(80.0);
        Assertions.assertEquals("80 %", formatted);

        // Test with decimal value
        formatted = TankMeasurementExcelGenerator.formatPorcentage(80.567);
        Assertions.assertEquals("81 %", formatted);
    }

    private TankMeasurementsDto createTankMeasurementsDto(LocalDateTime dateTime, String alarms, String tank,
                                                          Double productHeight, Double productVolume,
                                                          Double waterHeight, Double waterVolume,
                                                          Double productDensity, Double productMass,
                                                          Double tankFillingPercentage, Double temperature) {
        TankMeasurementsDto dto = TankMeasurementsDto.builder().build();
        dto.setDateTime(dateTime);
        dto.setAlarms(alarms);
        dto.setTank(Long.valueOf(tank));
        dto.setProductHeight(productHeight);
        dto.setProductVolume(productVolume);
        dto.setWaterHeight(waterHeight);
        dto.setWaterVolume(waterVolume);
        dto.setProductDensity(productDensity);
        dto.setProductMass(productMass);
        dto.setTankFillingPercentage(tankFillingPercentage);
        dto.setTemperature(temperature);
        return dto;
    }
}