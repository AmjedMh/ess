package com.teknokote.ess.core.service.export;

import com.teknokote.ess.core.model.EnumDeliveryStatus;
import com.teknokote.ess.core.service.impl.tank.TankDeliveryExcelGenerator;
import com.teknokote.ess.dto.TankDeliveryDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

class TankDeliveryExcelGeneratorTest {

    @Test
    void testGenerateDeliveryExcel() throws IOException {
        // Arrange
        List<TankDeliveryDto> tankDeliveriesDto = Arrays.asList(
                createTankDeliveryDto("2023-10-20T10:00", "2023-10-20T11:00", "1h", EnumDeliveryStatus.FINISH,
                        "1", "Diesel", 80.0, 100.0, 50.0, 20.0,
                        80.0, 100.0,20.0,20.0, 15.0),
                createTankDeliveryDto("2023-10-21T12:00", "2023-10-21T13:00", "1h", EnumDeliveryStatus.IN_PROGRESS,
                        "2", "Petrol", 120.0, 150.0,
                        70.0, 25.0, 120.0, 150.0,30.0, 10.0, 20.0)
        );
        List<String> columnsToDisplay = Arrays.asList("startDateTime", "endDateTime", "duration", "status", "tank", "fuelGradeName", "startProductVolume",
                "endProductVolume", "salesVolume", "productVolume", "startProductHeight", "endProductHeight", "productHeight", "waterHeight", "temperature");
        String locale = Locale.ENGLISH.toString();
        String filterSummary = "Test Summary";

        // Act
        byte[] excelData = TankDeliveryExcelGenerator.generateDeliveryExcel(tankDeliveriesDto, columnsToDisplay, locale, filterSummary);

        // Assert
        Assertions.assertNotNull(excelData);
        Assertions.assertTrue(excelData.length > 0);
    }
    @Test
    void testTranslateStatus() {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        String translated = TankDeliveryExcelGenerator.translateStatus(EnumDeliveryStatus.FINISH, bundle);
        Assertions.assertEquals("Finished", translated);
    }

    private TankDeliveryDto createTankDeliveryDto(String startDateTime, String endDateTime, String duration, EnumDeliveryStatus status, String tank, String fuelGradeName,
                                                  Double startProductVolume, Double endProductVolume, Double salesVolume, Double productVolume,
                                                  Double startProductHeight, Double endProductHeight, Double productHeight, Double waterHeight, Double temperature) {
        TankDeliveryDto dto = new TankDeliveryDto();
        dto.setStartDateTime(LocalDateTime.parse(startDateTime));
        dto.setEndDateTime(LocalDateTime.parse(endDateTime));
        dto.setDuration(duration);
        dto.setStatus(status);
        dto.setTank(Long.valueOf(tank));
        dto.setFuelGradeName(fuelGradeName);
        dto.setStartProductVolume(startProductVolume);
        dto.setEndProductVolume(endProductVolume);
        dto.setProductHeight(BigDecimal.valueOf(productHeight));
        dto.setSalesVolume(salesVolume);
        dto.setProductVolume(productVolume);
        dto.setStartProductHeight(BigDecimal.valueOf(startProductHeight));
        dto.setEndProductHeight(BigDecimal.valueOf(endProductHeight));
        dto.setWaterHeight(waterHeight);
        dto.setTemperature(BigDecimal.valueOf(temperature));
        return dto;
    }
}
