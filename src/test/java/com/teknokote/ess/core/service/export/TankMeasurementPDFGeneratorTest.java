package com.teknokote.ess.core.service.export;

import com.teknokote.ess.core.service.impl.tank.TankMeasurementPDFGenerator;
import com.teknokote.ess.dto.TankMeasurementsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TankMeasurementPDFGeneratorTest {

    @InjectMocks
    private TankMeasurementPDFGenerator pdfGenerator;
    @Mock
    private ResourceBundle resourceBundle;
    private TankMeasurementsDto tankMeasurementsDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Initialize the dto with test data
        tankMeasurementsDto = TankMeasurementsDto.builder()
                .dateTime(LocalDateTime.of(2023, 10, 1, 10, 0))
                .alarms("No alarms")
                .tank(1L)
                .productHeight(10.0)
                .productVolume(1000.0)
                .waterHeight(5.0)
                .waterVolume(200.0)
                .productDensity(0.85)
                .productMass(850.0)
                .tankFillingPercentage(80.0)
                .temperature(20.0)
                .build();
    }

    @Test
    void testGenerateMeasurementPDF_withValidData() {
        // Arrange
        List<TankMeasurementsDto> tankMeasurements = List.of(tankMeasurementsDto);
        List<String> columns = List.of("dateTime", "alarms", "productHeight", "productVolume");
        String locale = "en";
        String filterSummary = "date:2025-03-14, status:Active";

        // Mock behavior of ResourceBundle
        when(resourceBundle.containsKey("exportedOn")).thenReturn(true);
        when(resourceBundle.getString("exportedOn")).thenReturn("Exported On");

        // Act
        byte[] pdfBytes = TankMeasurementPDFGenerator.generateMeasurementPDF(tankMeasurements, columns, locale, filterSummary);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");
    }

    @Test
    void testGenerateMeasurementPDF_withEmptyColumns() {
        // Arrange
        List<TankMeasurementsDto> tankMeasurements = List.of(tankMeasurementsDto);
        List<String> columns = List.of(); // Empty columns list
        String locale = "en";
        String filterSummary = "";

        // Act
        byte[] pdfBytes = TankMeasurementPDFGenerator.generateMeasurementPDF(tankMeasurements, columns, locale, filterSummary);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF should not be empty");
    }

    @Test
    void testGenerateMeasurementPDF_withNoMeasurements() {
        // Arrange
        List<TankMeasurementsDto> tankMeasurements = List.of();
        List<String> columns = List.of("dateTime", "productHeight", "productVolume");
        String locale = "en";
        String filterSummary = "date:2025-03-14";

        // Act
        byte[] pdfBytes = TankMeasurementPDFGenerator.generateMeasurementPDF(tankMeasurements, columns, locale, filterSummary);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF should not be empty even if no measurements are provided");
    }
    @Test
    void testGetColumnValue_DateTime() {
        String result = TankMeasurementPDFGenerator.getColumnValue(tankMeasurementsDto, "dateTime");
        assertEquals("01/10/2023 10:00", result);
    }

    @Test
    void testGetColumnValue_Alarms() {
        String result = TankMeasurementPDFGenerator.getColumnValue(tankMeasurementsDto, "alarms");
        assertEquals("No alarms", result);
    }

    @Test
    void testGetColumnValue_Tank() {
        String result = TankMeasurementPDFGenerator.getColumnValue(tankMeasurementsDto, "tank");
        assertEquals("1", result);
    }

    @Test
    void testGetColumnValue_ProductHeight() {
        String result = TankMeasurementPDFGenerator.getColumnValue(tankMeasurementsDto, "productHeight");
        assertEquals("10", result); // Change to expected format as necessary.
    }

    @Test
    void testGetColumnValue_WaterHeight() {
        String result = TankMeasurementPDFGenerator.getColumnValue(tankMeasurementsDto, "waterHeight");
        assertEquals("5", result);
    }

    @Test
    void testGetColumnValue_WaterVolume() {
        String result = TankMeasurementPDFGenerator.getColumnValue(tankMeasurementsDto, "waterVolume");
        assertEquals("200", result);
    }

    @Test
    void testGetColumnValue_ProductDensity() {
        String result = TankMeasurementPDFGenerator.getColumnValue(tankMeasurementsDto, "productDensity");
        assertEquals("0.85", result);
    }

    @Test
    void testGetColumnValue_ProductMass() {
        String result = TankMeasurementPDFGenerator.getColumnValue(tankMeasurementsDto, "productMass");
        assertEquals("850.0", result);
    }

    @Test
    void testGetColumnValue_TankFillingPercentage() {
        String result = TankMeasurementPDFGenerator.getColumnValue(tankMeasurementsDto, "tankFillingPercentage");
        assertEquals("80 %", result); // Assuming formatPorcentage adds the "%" sign.
    }

    @Test
    void testGetColumnValue_Temperature() {
        String result = TankMeasurementPDFGenerator.getColumnValue(tankMeasurementsDto, "temperature");
        assertEquals("20,0 °C", result);
    }

    @Test
    void testGetColumnValue_InvalidColumn() {
        String result = TankMeasurementPDFGenerator.getColumnValue(tankMeasurementsDto, "invalidColumn");
        assertEquals("", result); // Expects empty string for invalid columns
    }

}
