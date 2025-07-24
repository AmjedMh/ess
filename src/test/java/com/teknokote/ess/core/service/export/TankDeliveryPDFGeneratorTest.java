package com.teknokote.ess.core.service.export;

import com.teknokote.ess.core.model.EnumDeliveryStatus;
import com.teknokote.ess.core.service.impl.tank.TankDeliveryPDFGenerator;
import com.teknokote.ess.dto.TankDeliveryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

class TankDeliveryPDFGeneratorTest {

    @Mock
    private ResourceBundle mockBundle;
    @Mock
    private TankDeliveryDto mockTankDeliveryDto;
    @Mock
    private List<TankDeliveryDto> tankDeliveriesDto;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockBundle.getString(anyString())).thenReturn("Mock String");

        tankDeliveriesDto = new ArrayList<>();
        TankDeliveryDto delivery1 = new TankDeliveryDto();
        delivery1.setStartDateTime(LocalDateTime.of(2023, 10, 1, 10, 0));
        delivery1.setEndDateTime(LocalDateTime.of(2023, 10, 1, 12, 0));
        delivery1.setDuration("2 hours");
        delivery1.setStatus(EnumDeliveryStatus.FINISH);
        delivery1.setTank(1L);
        delivery1.setFuelGradeName("Diesel");
        delivery1.setStartProductVolume(1000.0);
        delivery1.setEndProductVolume(800.0);
        delivery1.setSalesVolume(200.0);
        delivery1.setProductVolume(100.0);
        delivery1.setStartProductHeight(BigDecimal.valueOf(10.0));
        delivery1.setEndProductHeight(BigDecimal.valueOf(8.0));
        delivery1.setProductHeight(BigDecimal.valueOf(9.0));
        delivery1.setWaterHeight(1.0);
        delivery1.setTemperature(BigDecimal.valueOf(20.0));

        tankDeliveriesDto.add(delivery1);
    }
    @Test
    void testGenerateDeliveryPDF() {
        List<String> columnsToDisplay = Arrays.asList("startDateTime", "endDateTime", "duration", "status", "tank", "fuelGradeName");
        String locale = "en";
        String filterSummary = "Filter1: Value1, Filter2: Value2";

        // Generate the PDF
        byte[] pdfBytes = TankDeliveryPDFGenerator.generateDeliveryPDF(tankDeliveriesDto, columnsToDisplay, locale, filterSummary);

        // Assert that the PDF is generated (not null or empty)
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGetColumnValue() {
        TankDeliveryDto delivery = new TankDeliveryDto();
        delivery.setStartDateTime(LocalDateTime.of(2023, 10, 1, 10, 0));
        delivery.setEndDateTime(LocalDateTime.of(2023, 10, 1, 12, 0));
        delivery.setDuration("2 hours");
        delivery.setStatus(EnumDeliveryStatus.FINISH);
        delivery.setTank(1L);
        delivery.setFuelGradeName("Diesel");
        delivery.setStartProductVolume(1000.0);
        delivery.setEndProductVolume(800.0);
        delivery.setSalesVolume(200.0);
        delivery.setProductVolume(100.0);
        delivery.setStartProductHeight(BigDecimal.valueOf(10.0));
        delivery.setEndProductHeight(BigDecimal.valueOf(8.0));
        delivery.setProductHeight(BigDecimal.valueOf(9.0));
        delivery.setWaterHeight(1.0);
        delivery.setTemperature(BigDecimal.valueOf(20.0));

        // Test each column value
        assertEquals("01/10/2023 10:00", TankDeliveryPDFGenerator.getColumnValue(delivery, "startDateTime"));
        assertEquals("01/10/2023 12:00", TankDeliveryPDFGenerator.getColumnValue(delivery, "endDateTime"));
        assertEquals("2 hours", TankDeliveryPDFGenerator.getColumnValue(delivery, "duration"));
        assertEquals("1", TankDeliveryPDFGenerator.getColumnValue(delivery, "tank"));
        assertEquals("Diesel", TankDeliveryPDFGenerator.getColumnValue(delivery, "fuelGradeName"));
        assertEquals("800", TankDeliveryPDFGenerator.getColumnValue(delivery, "endProductVolume")); // Ensure formatVolume works
        assertEquals("200", TankDeliveryPDFGenerator.getColumnValue(delivery, "salesVolume")); // Ensure formatVolume works
        assertEquals("100", TankDeliveryPDFGenerator.getColumnValue(delivery, "productVolume")); // Ensure formatVolume works
        assertEquals("10", TankDeliveryPDFGenerator.getColumnValue(delivery, "startProductHeight")); // Ensure formatHeight works
        assertEquals("8", TankDeliveryPDFGenerator.getColumnValue(delivery, "endProductHeight")); // Ensure formatHeight works
        assertEquals("9", TankDeliveryPDFGenerator.getColumnValue(delivery, "productHeight")); // Ensure formatHeight works
        assertEquals("1.0", TankDeliveryPDFGenerator.getColumnValue(delivery, "waterHeight")); // Ensure formatting works
        assertEquals("20,0 °C", TankDeliveryPDFGenerator.getColumnValue(delivery, "temperature")); // Ensure formatting works
    }
    @Test
    void testGenerateDeliveryPDF_validData() {
        // Arrange
        List<String> columns = Arrays.asList("startDateTime", "endDateTime", "status");

        // Create tank deliveries using setters
        TankDeliveryDto tank1 = new TankDeliveryDto();
        tank1.setStartDateTime(LocalDateTime.parse("2025-03-14T12:00"));
        tank1.setEndDateTime(LocalDateTime.parse("2025-03-14T14:00"));
        tank1.setStatus(EnumDeliveryStatus.IN_PROGRESS);
        tank1.setWaterHeight(10.0);
        tank1.setTemperature(BigDecimal.valueOf(12.0));

        TankDeliveryDto tank2 = new TankDeliveryDto();
        tank2.setStartDateTime(LocalDateTime.parse("2025-03-14T15:00"));
        tank2.setEndDateTime(LocalDateTime.parse("2025-03-14T17:00"));
        tank2.setStatus(EnumDeliveryStatus.FINISH);
        tank2.setWaterHeight(15.0);
        tank2.setTemperature(BigDecimal.valueOf(18.0));

        List<TankDeliveryDto> tankDeliveries = Arrays.asList(tank1, tank2);
        String locale = "en";
        String filterSummary = "startDate: 2025-03-14";

        // Act
        byte[] pdfBytes = TankDeliveryPDFGenerator.generateDeliveryPDF(tankDeliveries, columns, locale, filterSummary);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "Generated PDF should not be empty");
    }

    @Test
    void testGenerateDeliveryPDF_noData() {
        // Arrange
        List<String> columns = Arrays.asList("startDateTime", "endDateTime", "status");
        List<TankDeliveryDto> tankDeliveries = Arrays.asList(); // No data
        String locale = "en";
        String filterSummary = "startDate: 2025-03-14";

        // Act
        byte[] pdfBytes = TankDeliveryPDFGenerator.generateDeliveryPDF(tankDeliveries, columns, locale, filterSummary);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "Generated PDF should not be empty, even with no data");
    }
    @Test
    void testGenerateDeliveryPDF_invalidLocale() {
        // Arrange
        List<String> columns = Arrays.asList("startDateTime", "endDateTime", "status");

        // Create a TankDeliveryDto object using setters
        TankDeliveryDto tankDelivery = new TankDeliveryDto();
        tankDelivery.setStartDateTime(LocalDateTime.parse("2025-03-14T12:00"));
        tankDelivery.setEndDateTime(LocalDateTime.parse("2025-03-14T14:00"));
        tankDelivery.setStatus(EnumDeliveryStatus.IN_PROGRESS);
        tankDelivery.setWaterHeight(10.0);
        tankDelivery.setTemperature(BigDecimal.valueOf(12.0));

        List<TankDeliveryDto> tankDeliveries = Arrays.asList(tankDelivery);

        String locale = "invalidLocale";
        String filterSummary = "startDate: 2025-03-14";

        // Act
        byte[] pdfBytes = TankDeliveryPDFGenerator.generateDeliveryPDF(tankDeliveries, columns, locale, filterSummary);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "Generated PDF should not be empty with invalid locale");
    }
    @Test
    void testGetColumnValue_validData() {
        // Arrange
        // Use DateTimeFormatter to match the format of '14/03/2025 12:00'
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Mock data
        when(mockTankDeliveryDto.getStartDateTime()).thenReturn(LocalDateTime.parse("14/03/2025 12:00", formatter));
        when(mockTankDeliveryDto.getEndDateTime()).thenReturn(LocalDateTime.parse("14/03/2025 14:00", formatter));
        when(mockTankDeliveryDto.getStatus()).thenReturn(EnumDeliveryStatus.valueOf("IN_PROGRESS"));

        // Act
        String startDateTimeValue = TankDeliveryPDFGenerator.getColumnValue(mockTankDeliveryDto, "startDateTime");
        String endDateTimeValue = TankDeliveryPDFGenerator.getColumnValue(mockTankDeliveryDto, "endDateTime");
        TankDeliveryPDFGenerator.getColumnValue(mockTankDeliveryDto, "status");

        // Assert
        assertEquals("14/03/2025 12:00", startDateTimeValue);
        assertEquals("14/03/2025 14:00", endDateTimeValue);
    }
}
