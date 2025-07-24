package com.teknokote.ess.core.service.controller;

import com.teknokote.ess.controller.front.StatistiqueController;
import com.teknokote.ess.core.model.movements.TankDelivery;
import com.teknokote.ess.core.service.impl.FuelGradesServiceImpl;
import com.teknokote.ess.core.service.impl.TankDeliveryService;
import com.teknokote.ess.core.service.impl.TankMeasurementServices;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.dto.SalesDto;
import com.teknokote.ess.dto.TankDeliveryDto;
import com.teknokote.ess.dto.charts.SalesGradesByPump;
import com.teknokote.ess.dto.charts.SalesGradesDto;
import com.teknokote.pts.client.upload.dto.MeasurementDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class StatistiqueControllerTest {

    @InjectMocks
    private StatistiqueController statistiqueController;
    @Mock
    private TransactionService transactionService;
    @Mock
    private FuelGradesServiceImpl fuelGradesService;
    @Mock
    private TankDeliveryService tankDeliveryService;
    @Mock
    private TankMeasurementServices tankMeasurementServices;

    @Test
    void testGetAllTankStat() {
        String ptsId = "0027003A3438510935383135";
        MeasurementDto measurementDto = new MeasurementDto();
        when(tankMeasurementServices.getAllTankMeasurementsSortedByTank(ptsId)).thenReturn(Collections.singletonList(measurementDto));

        List<MeasurementDto> result = statistiqueController.getAllTankStat(ptsId);

        assertEquals(1, result.size());
        verify(tankMeasurementServices).getAllTankMeasurementsSortedByTank(ptsId);
    }

    @Test
    void testGetLastDelivery() {
        Long idCtr = 1L;
        Long tank = 2L;
        TankDelivery tankDelivery = new TankDelivery();
        TankDeliveryDto expectedDto = new TankDeliveryDto();

        when(tankDeliveryService.findLatestTankDeliveryByTankId(idCtr, tank)).thenReturn(tankDelivery);
        when(tankDeliveryService.mapDeliveryToDto(tankDelivery)).thenReturn(expectedDto);

        TankDeliveryDto result = statistiqueController.getLastDelivery(idCtr, tank);

        assertEquals(expectedDto, result);
        verify(tankDeliveryService).findLatestTankDeliveryByTankId(idCtr, tank);
        verify(tankDeliveryService).mapDeliveryToDto(tankDelivery);
    }

    @Test
    void testGetSalesByController() {
        Long idCtr = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        // Initialize the SalesDto with example data
        BigDecimal totalAmountStart = BigDecimal.valueOf(500.00);
        BigDecimal totalAmountEnd = BigDecimal.valueOf(1000.00);
        double pumpSales = 250.50;
        double allSales = 750.75;
        Long pumpId = 2L;

        // Create SalesDto with values
        SalesDto salesDto = new SalesDto(totalAmountStart, totalAmountEnd, pumpSales, allSales, pumpId);

        when(transactionService.getSalesByController(idCtr, startDate, endDate))
                .thenReturn(Collections.singletonList(salesDto));

        List<SalesDto> result = statistiqueController.getSalesByController(idCtr, startDate, endDate);

        assertEquals(1, result.size());
        assertEquals(salesDto, result.get(0));
        verify(transactionService).getSalesByController(idCtr, startDate, endDate);
    }

    @Test
    void testGetSalesByPumpAndGrades() {
        Long idCtr = 1L;
        Long pumpId = 2L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        SalesGradesByPump salesGradesByPump = new SalesGradesByPump(1L,"Gasoil",100);
        when(transactionService.getSalesByGradesAndPump(idCtr, pumpId, startDate, endDate)).
                thenReturn(Collections.singletonList(salesGradesByPump));

        List<SalesGradesByPump> result = statistiqueController.getSalesByPumpAndGrades(idCtr, pumpId, startDate, endDate);

        assertEquals(1, result.size());
        verify(transactionService).getSalesByGradesAndPump(idCtr, pumpId, startDate, endDate);
    }

    @Test
    void testGetSalesGrades() {
        Long idCtr = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        SalesGradesDto salesGradesDto = new SalesGradesDto();
        when(fuelGradesService.getSalesByGrades(idCtr, startDate, endDate)).
                thenReturn(Collections.singletonList(salesGradesDto));

        List<SalesGradesDto> result = statistiqueController.getSalesGrades(idCtr, startDate, endDate);

        assertEquals(1, result.size());
        verify(fuelGradesService).getSalesByGrades(idCtr, startDate, endDate);
    }

}