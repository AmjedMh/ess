package com.teknokote.ess.core.service.controller;

import com.teknokote.ess.controller.front.ChartController;
import com.teknokote.ess.core.service.impl.TankMeasurementServices;
import com.teknokote.ess.core.service.impl.TankService;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.dto.TankConfigDto;
import com.teknokote.ess.dto.charts.ReportTankMeasurementAndLevelChartDto;
import com.teknokote.ess.dto.charts.TankLevelPerSalesChartDto;
import com.teknokote.ess.dto.charts.TankMeasurementChartDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ChartControllerTest {

    @InjectMocks
    private ChartController chartController;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TankService tankService;
    @Mock
    private TankMeasurementServices tankMeasurementServices;

    @Test
    void testChartTransaction() {
        // Setup
        Long idCtr = 1L;
        String fuel = "diesel";
        String pump = "pump1";
        String chartType = "amount";
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        when(transactionService.chartOfSalesType(idCtr, chartType, pump, fuel, startDate, endDate))
                .thenReturn(new ArrayList<>());

        // Execute
        List<?> result = chartController.chartTransaction(fuel, pump, chartType, startDate.toString(), endDate.toString(), idCtr);

        // Verify
        assertNotNull(result);
        verify(transactionService, times(1)).chartOfSalesType(idCtr, chartType, pump, fuel, startDate, endDate);
    }

    @Test
    void testChartTankLevelByDateTank() {
        // Setup
        Long idCtr = 1L;
        String tank = "1";
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        when(tankService.tankLevelChangesChart(idCtr, tank, startDate, endDate))
                .thenReturn(new ArrayList<>());

        // Execute
        List<TankLevelPerSalesChartDto> result = chartController.chartTankLevelByDateTank(idCtr, tank, startDate.toString(), endDate.toString());

        // Verify
        assertNotNull(result);
        verify(tankService, times(1)).tankLevelChangesChart(idCtr, tank, startDate, endDate);
    }
    @Test
    void testChartTankMeasurementByDateTank() {
        // Setup
        Long idCtr = 1L; // Example controller ID
        String tank = "1"; // Example tank ID
        LocalDateTime startDate = LocalDateTime.now().minusDays(1); // Start date
        LocalDateTime endDate = LocalDateTime.now(); // End date

        // Create a sample list of TankMeasurementChartDto
        List<TankMeasurementChartDto> measurementData = new ArrayList<>();
        // Assuming the TankMeasurementChartDto has a constructor like this:
        measurementData.add(new TankMeasurementChartDto(LocalDateTime.now().minusHours(1), 1L,100.0));
        measurementData.add(new TankMeasurementChartDto(LocalDateTime.now().minusHours(2), 1L,150.0));
        measurementData.add(new TankMeasurementChartDto(LocalDateTime.now().minusHours(3), 1L, 120.0));

        // Mock the service call
        when(tankMeasurementServices.reportTankMeasurementsByTankChart(idCtr, tank, startDate, endDate))
                .thenReturn(measurementData);
        // Execute
        List<TankMeasurementChartDto> result = chartController.chartTankMeasurementByDateTank(idCtr, tank, startDate.toString(), endDate.toString());

        // Verify
        assertNotNull(result); // Ensure result is not null
        assertEquals(measurementData.size(), result.size()); // Validate the size of the result
        verify(tankMeasurementServices, times(1)).reportTankMeasurementsByTankChart(idCtr, tank, startDate, endDate); // Verify the service method call
    }
    @Test
    void testChartTankMeasurementAndLevelByDateTank() {
        // Setup
        Long idCtr = 1L;
        String tank = "1";
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        when(tankMeasurementServices.reportTankMeasurementsByTankChart(idCtr, tank, startDate, endDate))
                .thenReturn(new ArrayList<>());
        when(tankService.tankLevelChangesChart(idCtr, tank, startDate, endDate))
                .thenReturn(new ArrayList<>());

        // Execute
        List<ReportTankMeasurementAndLevelChartDto> result = chartController.chartTankMeasurementAndLevelByDateTank(idCtr, tank, startDate.toString(), endDate.toString());

        // Verify
        assertNotNull(result);
        verify(tankMeasurementServices, times(1)).reportTankMeasurementsByTankChart(idCtr, tank, startDate, endDate);
        verify(tankService, times(1)).tankLevelChangesChart(idCtr, tank, startDate, endDate);
    }

    @Test
    void testGetAllTankByIdC() {
        // Setup
        Long idCtr = 1L;
        when(tankService.findTankByControllerOnCurrentConfiguration(idCtr)).thenReturn(new ArrayList<>());

        // Execute
        List<TankConfigDto> result = chartController.getAllTankByIdC(idCtr);

        // Verify
        assertNotNull(result);
        verify(tankService, times(1)).findTankByControllerOnCurrentConfiguration(idCtr);
    }
}
