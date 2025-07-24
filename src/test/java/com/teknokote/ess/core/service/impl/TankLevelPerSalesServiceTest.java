package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.*;
import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.core.model.movements.TankLevelPerSales;
import com.teknokote.ess.core.model.movements.TankMeasurement;
import com.teknokote.ess.core.repository.TankLevelPerSalesRepository;
import com.teknokote.ess.core.repository.tank_delivery.TankDeliveryRepository;
import com.teknokote.ess.core.repository.tank_measurement.TankMeasurementRepository;
import com.teknokote.ess.core.service.impl.transactions.TankLevelPerSalesService;
import com.teknokote.ess.dto.charts.TankLevelPerSalesChartDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TankLevelPerSalesServiceTest {

    @InjectMocks
    private TankLevelPerSalesService tankLevelPerSalesService;
    @Mock
    private TankLevelPerSalesRepository tankLevelPerSalesRepository;
    @Mock
    private TankDeliveryRepository tankDeliveryRepository;
    @Mock
    private TankMeasurementRepository tankMeasurementRepository;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLastTankLevelPerSales_ShouldReturnEmptyWhenNoRecordFound() {
        Long tankId = 1L;
        Long idCtr = 1L;

        when(tankLevelPerSalesRepository.getLastTankLevelPerSales(tankId, idCtr)).thenReturn(Optional.empty());

        Optional<TankLevelPerSales> result = tankLevelPerSalesService.getLastTankLevelPerSales(tankId, idCtr);
        assertFalse(result.isPresent());
    }

    @Test
    void getLastTankLevelPerSales_ShouldReturnTankLevelWhenRecordFound() {
        Long tankId = 1L;
        Long idCtr = 1L;

        TankLevelPerSales tankLevelPerSales = new TankLevelPerSales();
        tankLevelPerSales.setTankVolumeChanges(100.0);

        when(tankLevelPerSalesRepository.getLastTankLevelPerSales(tankId, idCtr)).thenReturn(Optional.of(tankLevelPerSales));

        Optional<TankLevelPerSales> result = tankLevelPerSalesService.getLastTankLevelPerSales(tankId, idCtr);
        assertTrue(result.isPresent());
        assertEquals(100.0, result.get().getTankVolumeChanges());
    }

    @Test
    void save_ShouldCallRepositorySave() {
        TankLevelPerSales tankLevelPerSales = new TankLevelPerSales();
        tankLevelPerSales.setTankVolumeChanges(150.0);

        when(tankLevelPerSalesRepository.save(any(TankLevelPerSales.class))).thenReturn(tankLevelPerSales);

        TankLevelPerSales savedTankLevelPerSales = tankLevelPerSalesService.save(tankLevelPerSales);

        assertEquals(150.0, savedTankLevelPerSales.getTankVolumeChanges());
        verify(tankLevelPerSalesRepository, times(1)).save(tankLevelPerSales);
    }

    @Test
    void findAllByControllerPtsIdAndTankPeriod_ShouldReturnListOfTankLevelPerSalesChartDto() {
        Long idCtr = 1L;
        String tank = "1";
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        when(tankLevelPerSalesRepository.findAllByControllerPtsIdAndTankPeriod(idCtr, tank, startDate, endDate)).thenReturn(Collections.emptyList());

        List<TankLevelPerSalesChartDto> result = tankLevelPerSalesService.findAllByControllerPtsIdAndTankPeriod(idCtr, tank, startDate, endDate);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findTankLevelPerSalesByPumpTransactionId_ShouldReturnTankLevelPerSales() {
        Long idCtr = 1L;
        String tank = "1";
        Long transactionId = 1L;

        TankLevelPerSales tankLevelPerSales = new TankLevelPerSales();
        tankLevelPerSales.setPumpTransactionId(transactionId);

        when(tankLevelPerSalesRepository.findTankLevelPerSalesByPumpTransactionId(idCtr, tank, transactionId)).thenReturn(tankLevelPerSales);

        TankLevelPerSales result = tankLevelPerSalesService.findTankLevelPerSalesByPumpTransactionId(idCtr, tank, transactionId);
        assertNotNull(result);
        assertEquals(transactionId, result.getPumpTransactionId());
    }

    @Test
    void getTankLeveChangesByTank_ShouldReturnListOfTankLevelChanges() {
        Long idCtr = 1L;
        String tankNo = "1";

        TankLevelPerSales tankLevelPerSales = new TankLevelPerSales();
        tankLevelPerSales.setTankVolumeChanges(80.0);

        when(tankLevelPerSalesRepository.getTankLeveChangesByTank(idCtr, tankNo)).thenReturn(Collections.singletonList(tankLevelPerSales));

        List<TankLevelPerSales> result = tankLevelPerSalesService.getTankLeveChangesByTank(idCtr, tankNo);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(80.0, result.get(0).getTankVolumeChanges());
    }

    @Test
    void saveTankLevelChanges_ShouldSaveNewTankLevelPerSales() {
        // Create a mock for PumpTransaction
        PumpTransaction pumpTransaction = mock(PumpTransaction.class);

        // Create a mock for Nozzle, Tank, and FuelGrade
        Nozzle mockNozzle = mockNozzle();
        ControllerPts mockControllerPts = mockControllerPts();
        FuelGrade mockFuelGrade = mock(FuelGrade.class);
        ControllerPtsConfiguration mockControllerPtsConfiguration = mock(ControllerPtsConfiguration.class);

        // Set up the mock behavior for the FuelGrade
        when(mockFuelGrade.getName()).thenReturn("Diesel");
        when(pumpTransaction.getFuelGrade()).thenReturn(mockFuelGrade);

        // Set up other mocked methods on PumpTransaction
        when(pumpTransaction.getNozzle()).thenReturn(mockNozzle);
        when(pumpTransaction.getControllerPts()).thenReturn(mockControllerPts);
        when(pumpTransaction.getControllerPtsConfiguration()).thenReturn(mockControllerPtsConfiguration);
        when(pumpTransaction.getVolume()).thenReturn(20.0);
        when(pumpTransaction.getDateTime()).thenReturn(LocalDateTime.now());

        // Mock previous tank volume scenario
        when(tankLevelPerSalesRepository.getLastTankLevelPerSales(anyLong(), anyLong())).thenReturn(Optional.of(mockTankLevelPerSales(50.0)));

        // Call the method under test
        tankLevelPerSalesService.saveTankLevelChanges(pumpTransaction);

        ArgumentCaptor<TankLevelPerSales> captor = ArgumentCaptor.forClass(TankLevelPerSales.class);
        verify(tankLevelPerSalesRepository, times(1)).save(captor.capture());

        TankLevelPerSales savedTankLevelPerSales = captor.getValue();
        assertNotNull(savedTankLevelPerSales);
        assertEquals(30.0, savedTankLevelPerSales.getTankVolumeChanges()); // 50 - 20 = 30
    }

    private Nozzle mockNozzle() {
        Nozzle nozzle = mock(Nozzle.class);
        Tank tank = mock(Tank.class);

        // Make sure this mocks Tank correctly
        when(tank.getIdConf()).thenReturn(1L);
        when(nozzle.getTank()).thenReturn(tank);
        return nozzle;
    }

    private ControllerPts mockControllerPts() {
        ControllerPts controllerPts = mock(ControllerPts.class);
        when(controllerPts.getId()).thenReturn(1L);
        return controllerPts;
    }

    private TankLevelPerSales mockTankLevelPerSales(double volume) {
        TankLevelPerSales tankLevelPerSales = new TankLevelPerSales();
        tankLevelPerSales.setTankVolumeChanges(volume);
        return tankLevelPerSales;
    }

    @Test
    void calculatePreviousTankVolume_ShouldReturnPreviousVolume_WhenEntryExists() {
        PumpTransaction pumpTransaction = mock(PumpTransaction.class);
        when(pumpTransaction.getVolume()).thenReturn(20.0); // Volume of product sold
        when(pumpTransaction.getDateTime()).thenReturn(LocalDateTime.now());

        // Mock Nozzle and Tank
        Nozzle mockNozzle = mock(Nozzle.class);
        Tank mockTank = mock(Tank.class);
        when(mockTank.getIdConf()).thenReturn(1L);
        when(mockNozzle.getTank()).thenReturn(mockTank);
        when(pumpTransaction.getNozzle()).thenReturn(mockNozzle);

        ControllerPts mockControllerPts = mockControllerPts();
        when(pumpTransaction.getControllerPts()).thenReturn(mockControllerPts);

        ControllerPtsConfiguration mockControllerPtsConfiguration = mock(ControllerPtsConfiguration.class);
        when(mockControllerPtsConfiguration.getControllerPts()).thenReturn(mockControllerPts);
        when(pumpTransaction.getControllerPtsConfiguration()).thenReturn(mockControllerPtsConfiguration);
        // Mocking FuelGrade to avoid NullPointerException
        FuelGrade mockFuelGrade = mock(FuelGrade.class);
        when(mockFuelGrade.getName()).thenReturn("Diesel");
        when(pumpTransaction.getFuelGrade()).thenReturn(mockFuelGrade);

        TankLevelPerSales previousTankLevel = new TankLevelPerSales();
        previousTankLevel.setTankVolumeChanges(50.0); // Previous volume is set to 50.0
        when(tankLevelPerSalesRepository.getLastTankLevelPerSales(anyLong(), anyLong())).thenReturn(Optional.of(previousTankLevel));
        // Call service method
        tankLevelPerSalesService.saveTankLevelChanges(pumpTransaction);

        // Verify that save was called and check if argument is correct
        ArgumentCaptor<TankLevelPerSales> captor = ArgumentCaptor.forClass(TankLevelPerSales.class);
        verify(tankLevelPerSalesRepository, times(1)).save(captor.capture());

        // Check expected volume after the sale
        assertEquals(30.0, captor.getValue().getTankVolumeChanges(), "The tank volume should be correctly calculated.");
    }

    // Test case where the latest TankLevelPerSales entry exists
    @Test
    void saveTankLevelChanges_ShouldUseLatestTankLevelPerSalesVolume() {
        PumpTransaction pumpTransaction = mock(PumpTransaction.class);
        Nozzle mockNozzle = mockNozzle();
        when(pumpTransaction.getNozzle()).thenReturn(mockNozzle);
        ControllerPts mockControllerPts = mockControllerPts();
        when(pumpTransaction.getControllerPts()).thenReturn(mockControllerPts);

        ControllerPtsConfiguration mockControllerPtsConfiguration = mock(ControllerPtsConfiguration.class);
        when(mockControllerPtsConfiguration.getControllerPts()).thenReturn(mockControllerPts);
        when(pumpTransaction.getControllerPtsConfiguration()).thenReturn(mockControllerPtsConfiguration);

        when(pumpTransaction.getVolume()).thenReturn(20.0);
        when(pumpTransaction.getDateTime()).thenReturn(LocalDateTime.now());
        // Mocking FuelGrade to avoid NullPointerException
        FuelGrade mockFuelGrade = mock(FuelGrade.class);
        when(mockFuelGrade.getName()).thenReturn("Diesel");
        when(pumpTransaction.getFuelGrade()).thenReturn(mockFuelGrade);


        // Mocking the last tank level entry
        TankLevelPerSales previousTankLevel = mockTankLevelPerSales(100.0);
        when(tankLevelPerSalesRepository.getLastTankLevelPerSales(anyLong(), anyLong()))
                .thenReturn(Optional.of(previousTankLevel));

        // Invoke the method under test
        tankLevelPerSalesService.saveTankLevelChanges(pumpTransaction);

        ArgumentCaptor<TankLevelPerSales> captor = ArgumentCaptor.forClass(TankLevelPerSales.class);
        verify(tankLevelPerSalesRepository, times(1)).save(captor.capture());

        // Previous volume 100.0 - Sold volume 20.0 should equal new volume 80.0
        assertEquals(80.0, captor.getValue().getTankVolumeChanges());
    }
    @Test
    void saveTankLevelChanges_ShouldUseEndProductVolumeWhenGreaterThanZero() {
        PumpTransaction pumpTransaction = mock(PumpTransaction.class);
        when(pumpTransaction.getVolume()).thenReturn(20.0);
        when(pumpTransaction.getDateTime()).thenReturn(LocalDateTime.now());

        // Create mock for Nozzle and Tank
        Nozzle mockNozzle = mockNozzle();
        when(pumpTransaction.getNozzle()).thenReturn(mockNozzle);
        ControllerPts mockControllerPts = mockControllerPts();
        when(pumpTransaction.getControllerPts()).thenReturn(mockControllerPts);

        ControllerPtsConfiguration mockControllerPtsConfiguration = mock(ControllerPtsConfiguration.class);
        when(mockControllerPtsConfiguration.getControllerPts()).thenReturn(mockControllerPts);
        when(pumpTransaction.getControllerPtsConfiguration()).thenReturn(mockControllerPtsConfiguration);

        // Mocking FuelGrade to avoid NullPointerException
        FuelGrade mockFuelGrade = mock(FuelGrade.class);
        when(mockFuelGrade.getName()).thenReturn("Diesel");
        when(pumpTransaction.getFuelGrade()).thenReturn(mockFuelGrade);

        TankMeasurement lastMeasurement = mock(TankMeasurement.class);
        when(lastMeasurement.getProductVolume()).thenReturn(130.0);
        when(tankMeasurementRepository.getLastMeasurementByDate(any(), any(),any())).thenReturn(Optional.of(lastMeasurement));
        when(tankLevelPerSalesRepository.getLastTankLevelPerSales(anyLong(), anyLong())).thenReturn(Optional.empty()); // No previous tank level
        // Call method
        tankLevelPerSalesService.saveTankLevelChanges(pumpTransaction);

        ArgumentCaptor<TankLevelPerSales> captor = ArgumentCaptor.forClass(TankLevelPerSales.class);
        verify(tankLevelPerSalesRepository, times(1)).save(captor.capture());

        assertEquals(110.0, captor.getValue().getTankVolumeChanges()); // Expect End Product Volume 130 - Sold Volume 20
    }

    @Test
    void saveTankLevelChanges_ShouldHandleNoPreviousMeasurement() {
        PumpTransaction pumpTransaction = mock(PumpTransaction.class);
        when(pumpTransaction.getVolume()).thenReturn(20.0);
        when(pumpTransaction.getDateTime()).thenReturn(LocalDateTime.now());

        // Create mock for Nozzle and Tank
        Nozzle mockNozzle = mockNozzle();
        when(pumpTransaction.getNozzle()).thenReturn(mockNozzle);
        ControllerPts mockControllerPts = mockControllerPts();
        when(pumpTransaction.getControllerPts()).thenReturn(mockControllerPts);

        // Mock case when no last tank level and no delivery
        when(tankLevelPerSalesRepository.getLastTankLevelPerSales(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(tankDeliveryRepository.getLastDelivery(anyLong(), any())).thenReturn(Optional.empty()); // ensure String is not used incorrectly

        // Call method
        tankLevelPerSalesService.saveTankLevelChanges(pumpTransaction);

        verify(tankLevelPerSalesRepository, never()).save(any()); // Ensure no save occurs
    }

}