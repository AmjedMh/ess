package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.PumpAttendantDao;
import com.teknokote.ess.core.dao.mappers.PumpAttendantMapper;
import com.teknokote.ess.core.dao.mappers.PumpTransactionMapper;
import com.teknokote.ess.core.model.Country;
import com.teknokote.ess.core.model.Currency;
import com.teknokote.ess.core.model.configuration.*;
import com.teknokote.ess.core.model.movements.EnumTransactionState;
import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.repository.configuration.ControllerPtsConfigurationRepository;
import com.teknokote.ess.core.repository.transactions.TransactionRepository;
import com.teknokote.ess.core.service.impl.transactions.TankLevelPerSalesService;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningExecutionService;
import com.teknokote.ess.core.service.shifts.WorkDayShiftPlanningService;
import com.teknokote.ess.dto.PumpAttendantDto;
import com.teknokote.ess.dto.SalesDto;
import com.teknokote.ess.dto.TransactionDto;
import com.teknokote.ess.dto.charts.ChartAllFuelAndAllPumpDto;
import com.teknokote.ess.dto.charts.ChartFuelAllPumpDto;
import com.teknokote.ess.dto.charts.FirstIndexSales;
import com.teknokote.ess.dto.charts.SalesGradesByPump;
import com.teknokote.ess.dto.data.FuelDataIndexStart;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDetailDto;
import com.teknokote.ess.dto.shifts.ShiftPlanningExecutionDto;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningDto;
import com.teknokote.ess.dto.shifts.WorkDayShiftPlanningExecutionDto;
import com.teknokote.pts.client.upload.pump.PumpUpload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class TransactionServiceTest {

   @InjectMocks
   private TransactionService transactionService;
   @Mock
   private TransactionRepository transactionRepository;
   @Mock
   private PumpTransactionMapper pumpTransactionMapper;
   @Mock
   private NozzleService nozzleService;
   @Mock
   private PumpService pumpService;
   @Mock
   private FuelGradesService fuelGradesService;
   @Mock
   private PumpAttendantDao pumpAttendantDao;
   @Mock
   private PumpAttendantMapper mapper;
   @Mock
   private WorkDayShiftPlanningService workDayShiftPlanningService;
   @Mock
   private WorkDayShiftPlanningExecutionService workDayShiftPlanningExecutionService;
   private ControllerPtsConfiguration controllerPtsConfiguration;
   @Mock
   private TankLevelPerSalesService tankLevelPerSalesService;
   @Mock
   private ControllerPtsConfigurationRepository controllerPtsConfigurationRepository;
   private PumpUpload pumpUpload;
   private Long pumpId = 1L;
   private Long idCtr = 1L;
   private Long fuelId = 1L;
   private LocalDateTime startDate = LocalDateTime.of(2025, 3, 1, 0, 0, 0, 0);
   private LocalDateTime endDate = LocalDateTime.of(2025, 3, 2, 0, 0, 0, 0);
   @BeforeEach
   void setUp() {

      controllerPtsConfiguration = new ControllerPtsConfiguration();
      controllerPtsConfiguration.setPtsId("0027003A3438510935383135");

      ControllerPts controllerPts = new ControllerPts();
      controllerPts.setStationId(1L);
      controllerPtsConfiguration.setControllerPts(controllerPts);

      pumpUpload = new PumpUpload();
      pumpUpload.setTransaction(123L);
      pumpUpload.setPump(1L);
      pumpUpload.setNozzle(1L);
      pumpUpload.setTotalVolume(100.0);
      pumpUpload.setConfigurationId("150e9c08");
      pumpUpload.setFuelGradeId(1L);
      pumpUpload.setTag("610HS8990");

      // Initialize pumpTransaction with default values
      PumpTransaction pumpTransaction = new PumpTransaction();
      pumpTransaction.setDateTime(LocalDateTime.now());
      Pump pump = new Pump();
      pump.setId(1L);
      pumpTransaction.setPump(pump);
      Nozzle nozzle = new Nozzle();
      nozzle.setId(1L);
      pumpTransaction.setNozzle(nozzle);

      controllerPtsConfigurationRepository = mock(ControllerPtsConfigurationRepository.class);
      transactionService = new TransactionService(transactionRepository,
              controllerPtsConfigurationRepository,
              workDayShiftPlanningService,
              nozzleService,
              pumpService,
              workDayShiftPlanningExecutionService,
              mapper,
              pumpTransactionMapper,
              fuelGradesService,
              pumpAttendantDao,
              tankLevelPerSalesService);
   }
   @Test
   void mapToPumpTransactionDto_ShouldReturnCorrectDto() {
      // Arrange
      PumpTransaction pumpTransaction = new PumpTransaction();

      // Mock and set pump
      Pump pump = new Pump();
      pump.setIdConf(1L);
      pumpTransaction.setPump(pump);

      // Mock and set nozzle
      Nozzle nozzle = new Nozzle();
      nozzle.setIdConf(2L);
      pumpTransaction.setNozzle(nozzle);

      // Set transaction details
      pumpTransaction.setTransactionReference(12345L);
      pumpTransaction.setVolume(100.0);
      pumpTransaction.setAmount(500.0);
      pumpTransaction.setTotalVolume(BigDecimal.valueOf(200.0));
      pumpTransaction.setTotalAmount(BigDecimal.valueOf(1000.0));
      pumpTransaction.setTag("TAG1");

      // Mock PumpAttendant
      PumpAttendant pumpAttendant = new PumpAttendant();
      pumpAttendant.setFirstName("John");
      pumpAttendant.setLastName("Doe");
      pumpTransaction.setPumpAttendant(pumpAttendant);

      // Mock FuelGrade
      FuelGrade fuelGrade = new FuelGrade();
      fuelGrade.setName("Super");
      pumpTransaction.setFuelGrade(fuelGrade);

      // Mock ControllerPtsConfiguration
      controllerPtsConfiguration = new ControllerPtsConfiguration();
      ControllerPts controllerPts = new ControllerPts();

      // Mock the Station object if needed
      Station station = new Station();
      Country country = new Country();

      // Create and set Currency
      Currency currency = new Currency();
      currency.setCode("USD");
      country.setCurrency(currency);

      // Set up hierarchy
      station.setCountry(country);
      controllerPts.setStation(station);
      controllerPtsConfiguration.setControllerPts(controllerPts);

      // Set the controllerPtsConfiguration in PumpTransaction
      pumpTransaction.setControllerPtsConfiguration(controllerPtsConfiguration);

      // Act
      TransactionDto transactionDto = transactionService.mapToPumpTransactionDto(pumpTransaction);

      assertEquals("USD", transactionDto.getDevise());
      assertEquals("John Doe", transactionDto.getPumpAttendantName());
      assertEquals("Super", transactionDto.getFuelGradeName());
      }

   /****************** Test Transaction Finished ******************/
   @Test
   void processUploadedTransactionPacket_ExistingTransaction_Finished() {
      // Setup mock behavior for existing transaction
      PumpTransaction finishedTransaction = new PumpTransaction();
      finishedTransaction.setTransactionReference(123L);
      finishedTransaction.setState(EnumTransactionState.FINISHED); // Already finished

      when(transactionRepository.findByReferenceAndPumpAndNozzleAndPtsIdAndTotalVolume(
              123L, 1L, 1L,
              controllerPtsConfiguration.getPtsId(),
              BigDecimal.valueOf(100.0)
      )).thenReturn(Optional.of(finishedTransaction));

      // Perform the call
      transactionService.processUploadedTransactionPacket(pumpUpload, controllerPtsConfiguration);

      // Verify that no save or update is attempted
      verify(transactionRepository, never()).save(any());
      verify(pumpTransactionMapper, never()).updateAndMarkAsFinished(any(), any());

      // Add assertions to verify the transaction state remains unchanged
      assertEquals(EnumTransactionState.FINISHED, finishedTransaction.getState());
      assertNotNull(finishedTransaction.getTransactionReference());
   }

   /************************ Test Calculate FuelGrade Sales ***************/
   @Test
   void testCalculateGradeSales_WithValidAmounts() {
   // Given
   BigDecimal endAmount = new BigDecimal("100.00");
   FirstIndexSales firstIndexAmount = new FirstIndexSales(10, new BigDecimal("50.00"));

   // Mocking the repository methods
   when(transactionRepository.findTotalAmountEndForGradeAndPump(pumpId, idCtr, fuelId, startDate, endDate))
           .thenReturn(endAmount);
   when(transactionRepository.findTotalAmountStartForGradeAndPumpByDate(pumpId, idCtr, fuelId, startDate, endDate))
           .thenReturn(firstIndexAmount);

   // When
   double result = transactionService.calculateGradeSales(pumpId, idCtr, fuelId, startDate, endDate);

   // Then
   assertEquals(60.00, result, 0.01); // 100 - (50 - 10) = 60
}

   @Test
    void testCalculateGradeSales_WithNullEndAmount() {
      // Given
      when(transactionRepository.findTotalAmountEndForGradeAndPump(pumpId, idCtr, fuelId, startDate, endDate))
              .thenReturn(null);

      // When
      double result = transactionService.calculateGradeSales(pumpId, idCtr, fuelId, startDate, endDate);

      // Then
      assertEquals(0.0, result, 0.01); // Should return 0.0 when endAmount is null
   }

   @Test
    void testCalculateGradeSales_WithZeroEndAmount() {
      // Given
      BigDecimal endAmount = BigDecimal.ZERO;
      when(transactionRepository.findTotalAmountEndForGradeAndPump(pumpId, idCtr, fuelId, startDate, endDate))
              .thenReturn(endAmount);

      // When
      double result = transactionService.calculateGradeSales(pumpId, idCtr, fuelId, startDate, endDate);

      // Then
      assertEquals(0.0, result, 0.01); // Should return 0.0 when endAmount is zero
   }

   @Test
    void testCalculateGradeSales_WithZeroSales() {
      // Given
      BigDecimal endAmount = new BigDecimal("100.00");
      FirstIndexSales firstIndexAmount = new FirstIndexSales(0, new BigDecimal("100.00"));

      when(transactionRepository.findTotalAmountEndForGradeAndPump(pumpId, idCtr, fuelId, startDate, endDate))
              .thenReturn(endAmount);
      when(transactionRepository.findTotalAmountStartForGradeAndPumpByDate(pumpId, idCtr, fuelId, startDate, endDate))
              .thenReturn(firstIndexAmount);
      when(transactionRepository.findLastAmountForGradeAndPump(pumpId, idCtr, fuelId, startDate, endDate))
              .thenReturn(Double.valueOf("10.00"));

      // When
      double result = transactionService.calculateGradeSales(pumpId, idCtr, fuelId, startDate, endDate);

      // Then
      assertEquals(10.0, result, 0.01); // Should return 10.0 as fallback when sales are 0
   }
   @Test
    void testGetSalesByGradesAndPump_InvalidDates() {
      // Given
      startDate = null;
      endDate = null;

      // When & Then
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
         transactionService.getSalesByGradesAndPump(idCtr, pumpId, startDate, endDate);
      });
      assertEquals("startDate and endDate must not be null", exception.getMessage());
   }

   /********************** Test Find PumpAttendant For Transaction ****************/
   @Test
   void testFindPumpAttendantForTransaction_Success() {
      WorkDayShiftPlanningDto workDayShiftPlanningDto = WorkDayShiftPlanningDto.builder()
              .id(1L)
              .day(LocalDate.now())
              .build();

      WorkDayShiftPlanningExecutionDto executionDto = WorkDayShiftPlanningExecutionDto.builder()
              .shiftPlanningExecutions(new HashSet<>())
              .build();

      ShiftPlanningExecutionDto shiftExecutionDto = ShiftPlanningExecutionDto.builder()
              .startDateTime(LocalDateTime.now().minusHours(1))
              .endDateTime(LocalDateTime.now().plusHours(1))
              .shiftPlanningExecutionDetail(new ArrayList<>())
              .build();

      Pump pump = new Pump();
      pump.setId(1L);
      pump.setIdConf(1L);
      Nozzle nozzle = new Nozzle();
      nozzle.setId(1L);
      nozzle.setIdConf(1L);

      PumpAttendantDto attendantDto = PumpAttendantDto.builder()
              .id(100L)
              .version(1L)
              .actif(true)
              .dateStatusChange(LocalDateTime.now())
              .firstName("John")
              .lastName("Doe")
              .address("123 Street")
              .tag("TAG123")
              .stationId(1L)
              .matricule("MAT123")
              .phone("0612345678")
              .build();

      ShiftPlanningExecutionDetailDto detailDto = ShiftPlanningExecutionDetailDto.builder()
              .pump(pump)
              .nozzle(nozzle)
              .pumpAttendant(attendantDto)
              .forced(false)
              .build();

      shiftExecutionDto.setShiftPlanningExecutionDetail(List.of(detailDto));
      executionDto.setShiftPlanningExecutions(Set.of(shiftExecutionDto));

      when(workDayShiftPlanningService.findByStationAndDay(1L, LocalDate.now()))
              .thenReturn(workDayShiftPlanningDto);

      when(workDayShiftPlanningExecutionService.findByWorkDay(workDayShiftPlanningDto.getId()))
              .thenReturn(Optional.of(executionDto));

      when(mapper.toEntity(any(PumpAttendantDto.class))).thenReturn(new PumpAttendant());

      PumpTransaction pumpTransaction = new PumpTransaction();
      pumpTransaction.setDateTime(LocalDateTime.now());
      pumpTransaction.setPump(pump);
      pumpTransaction.setNozzle(nozzle);

      // Call the method under test
      transactionService.findPumpAttendantForTransaction(pumpTransaction, controllerPtsConfiguration);

      // Assertions to check that the pump attendant was found and assigned correctly
      assertNotNull(pumpTransaction.getPumpAttendant());
      assertEquals(100L, pumpTransaction.getPumpAttendant().getId());
      assertEquals(1, pumpTransaction.getPumpAttendant().getVersion());
   }

   /********************** Test Chart Of Sales Amount *******************/

   @Test
   void chartOfSalesAmount_ShouldThrowException_WhenStartDateIsNull() {
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
              transactionService.chartOfSalesAmount(idCtr, "ALL", "ALL", null, endDate));
      assertEquals("startDate and endDate must not be null", exception.getMessage());
   }

   @Test
   void chartOfSalesAmount_ShouldReturnEmptyList_WhenNoConfigurationFound() {
      when(controllerPtsConfigurationRepository.findCurrentConfigurationOnController(idCtr)).thenReturn(Optional.empty());

      List<ChartAllFuelAndAllPumpDto> result = transactionService.chartOfSalesAmount(idCtr, "ALL", "ALL", startDate, endDate);

      assertTrue(result.isEmpty());
   }

   @Test
   void chartOfSalesAmount_ShouldReturnSalesData_WhenValidDataProvided() {
      when(controllerPtsConfigurationRepository.findCurrentConfigurationOnController(idCtr)).thenReturn(Optional.of(controllerPtsConfiguration));
      when(pumpService.findPumpIdsByControllerConfiguration(controllerPtsConfiguration.getConfigurationId(), controllerPtsConfiguration.getPtsId()))
              .thenReturn(Arrays.asList(1L, 2L));
      when(nozzleService.findAllFuelIdsByConfigurationAndPumpId(1L, controllerPtsConfiguration.getConfigurationId(), controllerPtsConfiguration.getPtsId()))
              .thenReturn(Arrays.asList(101L, 102L));
      when(nozzleService.findAllFuelIdsByConfigurationAndPumpId(2L, controllerPtsConfiguration.getConfigurationId(), controllerPtsConfiguration.getPtsId()))
              .thenReturn(Arrays.asList(201L));

      List<Object[]> pump1Results = new ArrayList<>();
      pump1Results.add(new Object[]{null, null, null, "2024-01", null, null, null, 500.0});
      List<Object[]> pump2Results = new ArrayList<>();
      pump2Results.add(new Object[]{null, null, null, "2024-01", null, null, null, 500.0});

      when(transactionRepository.getAggregatedAmountSales(eq(idCtr), any(), any(), eq(1L), anyLong(), anyString()))
              .thenReturn(pump1Results);
      when(transactionRepository.getAggregatedAmountSales(eq(idCtr), any(), any(), eq(2L), anyLong(), anyString()))
              .thenReturn(pump2Results);

      List<ChartAllFuelAndAllPumpDto> result = transactionService.chartOfSalesAmount(idCtr, "ALL", "ALL", startDate, endDate);

      assertFalse(result.isEmpty());
      assertEquals(1, result.size());
      assertEquals("2024-01", result.get(0).getDate());
      assertEquals(1500.0, result.get(0).getSum());
   }

   /********************** Test Chart Of Sales Volume *******************/
   @Test
   void testChartOfSalesVolume_AllPumpsAllFuels() {
      ControllerPtsConfiguration config = new ControllerPtsConfiguration();
      config.setConfigurationId("12Ah374");
      config.setPtsId("0027003A3438510935383135");

      when(controllerPtsConfigurationRepository.findCurrentConfigurationOnController(idCtr))
              .thenReturn(Optional.of(config));

      List<Long> pumpIds = Arrays.asList(1L, 2L);
      when(pumpService.findPumpIdsByControllerConfiguration(config.getConfigurationId(), config.getPtsId()))
              .thenReturn(pumpIds);

      Map<Long, List<String>> pumpToFuelMap = new HashMap<>();
      pumpToFuelMap.put(1L, Arrays.asList("Gasoil", "Super 91"));
      pumpToFuelMap.put(2L, Arrays.asList("Gasoil"));

      when(nozzleService.findAllFuelByConfigurationAndPumpId(1L, config.getConfigurationId(), config.getPtsId()))
              .thenReturn(Arrays.asList("Gasoil", "Super 91"));
      when(nozzleService.findAllFuelByConfigurationAndPumpId(2L, config.getConfigurationId(), config.getPtsId()))
              .thenReturn(Arrays.asList("Gasoil"));

      // Mock aggregated sales for pump 1 and pump 2
      when(transactionRepository.getAggregatedVolumeSales(eq(idCtr), any(), any(), eq(1L), eq("Gasoil"), anyString()))
              .thenReturn(Arrays.<Object[]>asList(new Object[]{"Gasoil", "2024-01", 500.0}));
      when(transactionRepository.getAggregatedVolumeSales(eq(idCtr), any(), any(), eq(1L), eq("Super 91"), anyString()))
              .thenReturn(Arrays.<Object[]>asList(new Object[]{"Super 91", "2024-01", 700.0}));
      when(transactionRepository.getAggregatedVolumeSales(eq(idCtr), any(), any(), eq(2L), eq("Gasoil"), anyString()))
              .thenReturn(Arrays.<Object[]>asList(new Object[]{"Gasoil", "2024-01", 500.0}));

      // Call the service method
      List<ChartFuelAllPumpDto> result = transactionService.chartOfSalesVolume(idCtr, "ALL", "ALL", startDate, endDate);

      // Verify results
      assertNotNull(result);
      assertEquals(2, result.size());

      // Convert result to a map for easy assertions
      Map<String, Double> resultMap = result.stream()
              .collect(Collectors.toMap(dto -> dto.getNameF() + "-" + dto.getDateF(), ChartFuelAllPumpDto::getSumF));

      // Check if the aggregated sales values are as expected
      assertEquals(1000.0, resultMap.get("Gasoil-2024-01"));  // 500.0 from pump 1 + 500.0 from pump 2
      assertEquals(700.0, resultMap.get("Super 91-2024-01")); // 700.0 from pump 1
   }

   /****************** Test Chart By Controller ********************/
   @Test
    void testGetSalesByControllerWithNoConfig() {
      when(controllerPtsConfigurationRepository.findCurrentConfigurationOnController(anyLong()))
              .thenReturn(Optional.empty());

      List<SalesDto> result = transactionService.getSalesByController(1L, LocalDateTime.now(), LocalDateTime.now());

      assertTrue(result.isEmpty());
   }
   @Test
   void testChartOfSalesType_InvalidChartType() {
      // Arrange
      idCtr = 1L;
      String chartType = "INVALID_TYPE";
      String pump = "1";
      String fuel = "Gasoile";
      startDate = LocalDateTime.now().minusDays(1);
      endDate = LocalDateTime.now();

      // Act & Assert
      IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
         transactionService.chartOfSalesType(idCtr, chartType, pump, fuel, startDate, endDate);
      });

      assertEquals("Invalid chartType", thrown.getMessage());
   }
   @Test
   void testProcessChartAmountSales_AllPumpsAndAllFuels() {
      // Arrange
      idCtr = 1L;
      startDate = LocalDateTime.now().minusMonths(1);
      endDate = LocalDateTime.now();
      Map<Long, List<Long>> pumpToFuelIdsMap = new HashMap<>();
      pumpToFuelIdsMap.put(1L, Arrays.asList(100L, 200L));
      String unit = "someUnit";

      // Mocking the behavior of the repository
      when(transactionRepository.getAggregatedAmountSales(
              eq(idCtr), eq(startDate), eq(endDate), anyLong(), anyLong(), eq(unit)))
              .thenReturn(Arrays.asList(
                      new Object[]{null, null, null, "2024-01", 0.0, null, null, 100.0},
                      new Object[]{null, null, null, "2024-01", 0.0, null, null, 200.0}
              ));

      // Act
      List<ChartAllFuelAndAllPumpDto> result = transactionService.processChartAmountSales(idCtr, "ALL", "ALL", startDate, endDate, pumpToFuelIdsMap, unit);

      // Assert
      assertEquals(1, result.size());
   }
   @Test
   void testProcessChartAmountSales_SpecificPumpAndAllFuels() {
      // Arrange
      idCtr = 2L;
      startDate = LocalDateTime.now().minusMonths(1);
      endDate = LocalDateTime.now();
      Long specificPump = 1L;
      Map<Long, List<Long>> pumpToFuelIdsMap = new HashMap<>();
      pumpToFuelIdsMap.put(specificPump, Arrays.asList(100L, 200L));
      String unit = "Month";

      // Mock repository behavior
      when(transactionRepository.getAggregatedAmountSales(
              eq(idCtr), eq(startDate), eq(endDate), eq(specificPump), anyLong(), eq(unit)))
              .thenReturn(Arrays.asList(
                      new Object[]{null, null, null, "2024-01", 0.0, null, null, 150.0},
                      new Object[]{null, null, null, "2024-02", 0.0, null, null, 200.0}
              ));

      // Act
      List<ChartAllFuelAndAllPumpDto> result = transactionService.processChartAmountSales(idCtr, specificPump.toString(), "ALL", startDate, endDate, pumpToFuelIdsMap, unit);

      // Assert
      assertEquals(2, result.size());
   }
   @Test
   void getSalesByController_ShouldThrowIllegalArgumentException_WhenStartDateOrEndDateIsNull() {
      // Assert that passing a null end date throws an exception
      assertThrows(IllegalArgumentException.class,
              () -> invokeGetSalesByController(1L, null, LocalDateTime.now()));

      // Assert that passing a null start date throws an exception
      assertThrows(IllegalArgumentException.class,
              () -> invokeGetSalesByController(1L, LocalDateTime.now(), null));
   }

   // Helper method for calling getSalesByController
   private void invokeGetSalesByController(Long controllerId, LocalDateTime startDate, LocalDateTime endDate) {
      transactionService.getSalesByController(controllerId, startDate, endDate);
   }
   @Test
   void getSalesByController_ShouldReturnEmptyList_WhenNoControllerConfig() {
      // Set up the mock to return empty configuration for a different controller ID
      when(controllerPtsConfigurationRepository.findCurrentConfigurationOnController(anyLong()))
              .thenReturn(Optional.empty());

      // Call the service method with a different timing
      startDate = LocalDateTime.now().minusDays(1);
      endDate = LocalDateTime.now();

      List<SalesDto> result = transactionService.getSalesByController(2L, startDate, endDate);

      // Assert that the result is empty
      assertTrue(result.isEmpty(), "Expected empty sales list when no configuration exists for the controller.");
   }
   @Test
   void getSalesByGradesAndPump_ShouldThrowException_WhenDatesAreNull() {
      // Act & Assert
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
         transactionService.getSalesByGradesAndPump(1L, 1L, null, null);
      });
      assertEquals("startDate and endDate must not be null", exception.getMessage());
   }
   @Test
   void getSalesByGradesAndPump_ShouldReturnNull_WhenConfigurationNotFound() {
      Long controllerId = 1L;
      pumpId = 1L;
      startDate = LocalDateTime.now().minusDays(1);
      endDate = LocalDateTime.now();

      when(controllerPtsConfigurationRepository.findCurrentConfigurationOnController(controllerId))
              .thenReturn(Optional.empty());

      // Act
      List<SalesGradesByPump> result = transactionService.getSalesByGradesAndPump(controllerId, pumpId, startDate, endDate);

      // Assert
      assertNull(result, "Expected null result when configuration not found");
   }
   @Test
   void testFindInitialIndex() {
      pumpId = 1L;
      String ptsId = "ptsId";
      Long fuleId = 1L;
      startDate = LocalDateTime.now();

      FuelDataIndexStart expectedFuelDataIndex = new FuelDataIndexStart(BigDecimal.valueOf(0.0),BigDecimal.valueOf(0.0));

      when(transactionRepository.findInitialIndex(eq(pumpId), eq(ptsId), eq(fuleId), any(LocalDateTime.class)))
              .thenReturn(expectedFuelDataIndex);

      // Act: Call the service method
      FuelDataIndexStart result = transactionService.findInitialIndex(pumpId, ptsId, fuleId, startDate);

      // Assert: Verify the result
      assertNotNull(result);
      assertSame(expectedFuelDataIndex, result, "Returned FuelDataIndexStart should match the expected one");
   }
   @Test
   void testFindInitialIndex_notFound() {
      pumpId = 1L;
      String ptsId = "ptsId";
      Long fuleId = 1L;
      startDate = LocalDateTime.now();

      when(transactionRepository.findInitialIndex(eq(pumpId), eq(ptsId), eq(fuleId), any(LocalDateTime.class)))
              .thenReturn(null);

      // Act: Call the service method
      FuelDataIndexStart result = transactionService.findInitialIndex(pumpId, ptsId, fuleId, startDate);

      // Assert: Verify that the result is null
      assertNull(result, "Expected null to be returned when no initial index is found");
   }
   @Test
   void testFindLastTransactionOnDateByTag() {
      PumpTransaction expectedTransaction = new PumpTransaction();

      String ptsId = "0027003A3438510935383135";
      Long nozzleId = 1L;
      pumpId = 1L;
      String tag = "D875HK";
      LocalDateTime dateTime = LocalDateTime.now();

      when(transactionRepository.findLastTransactionOnDateByTag(eq(ptsId), eq(nozzleId), eq(pumpId), eq(tag), any(LocalDateTime.class)))
              .thenReturn(Optional.of(expectedTransaction));

      // Act: Call the service method
      Optional<PumpTransaction> result = transactionService.findLastTransactionOnDateByTag(ptsId, nozzleId, pumpId, tag, dateTime);

      // Assert: Verify the result
      assertTrue(result.isPresent(), "Expected a transaction to be returned");
      assertSame(expectedTransaction, result.get(), "Returned transaction should match the expected one");
   }
   @Test
   void testFindLastTransactionOnDateByTag_notFound() {

      String ptsId = "0027003A3438510935383135";
      Long nozzleId = 1L;
      pumpId = 1L;
      String tag = "D875HK";
      LocalDateTime dateTime = LocalDateTime.now();

      when(transactionRepository.findLastTransactionOnDateByTag(eq(ptsId), eq(nozzleId), eq(pumpId), eq(tag), any(LocalDateTime.class)))
              .thenReturn(Optional.empty());

      // Act: Call the service method
      Optional<PumpTransaction> result = transactionService.findLastTransactionOnDateByTag(ptsId, nozzleId, pumpId, tag, dateTime);

      // Assert: Verify the result is empty
      assertFalse(result.isPresent(), "Expected no transaction to be returned");
   }
   @Test
   void testFindLastTransactionOnDate() {
      PumpTransaction expectedTransaction = new PumpTransaction();

      String ptsId = "0027003A3438510935383135";
      Long nozzleId = 1L;
      pumpId = 1L;
      LocalDateTime dateTime = LocalDateTime.now();

      when(transactionRepository.findLastTransactionOnDate(eq(ptsId), eq(nozzleId), eq(pumpId), any(LocalDateTime.class)))
              .thenReturn(Optional.of(expectedTransaction));

      // Act: Call the service method
      Optional<PumpTransaction> result = transactionService.findLastTransactionOnDate(ptsId, nozzleId, pumpId, dateTime);

      // Assert: Verify the result
      assertTrue(result.isPresent(), "Expected a transaction to be returned");
      assertSame(expectedTransaction, result.get(), "Returned transaction should match the expected one");
   }
   @Test
   void testFindLastTransactionOnDate_notFound() {

      String ptsId = "0027003A3438510935383135";
      Long nozzleId = 1L;
      pumpId = 1L;
      LocalDateTime dateTime = LocalDateTime.now();

      when(transactionRepository.findLastTransactionOnDate(eq(ptsId), eq(nozzleId), eq(pumpId), any(LocalDateTime.class)))
              .thenReturn(Optional.empty());

      // Act: Call the service method
      Optional<PumpTransaction> result = transactionService.findLastTransactionOnDate(ptsId, nozzleId, pumpId, dateTime);

      // Assert: Verify that the result is empty
      assertFalse(result.isPresent(), "Expected no transaction to be returned");
   }
   @Test
   void testFindFirstTransactionOnDate() {
      PumpTransaction expectedTransaction = new PumpTransaction();

      String ptsId = "0027003A3438510935383135";
      Long nozzleId = 1L;
      pumpId = 1L;
      LocalDateTime dateTime = LocalDateTime.now();

      when(transactionRepository.findFirstTransactionOnDate(eq(ptsId), eq(nozzleId), eq(pumpId), any(LocalDateTime.class)))
              .thenReturn(Optional.of(expectedTransaction));

      // Act: Call service method
      Optional<PumpTransaction> result = transactionService.findFirstTransactionOnDate(ptsId, nozzleId, pumpId, dateTime);

      // Assert: Verify the returned transaction is as expected
      assertTrue(result.isPresent(), "Expected a transaction to be returned");
      assertSame(expectedTransaction, result.get(), "Returned transaction should match the expected one");
   }

   @Test
   void testFindFirstTransactionOnDate_notFound() {
      String ptsId = "0027003A3438510935383135";
      Long nozzleId = 1L;
      pumpId = 1L;
      LocalDateTime dateTime = LocalDateTime.now();

      when(transactionRepository.findFirstTransactionOnDate(eq(ptsId), eq(nozzleId), eq(pumpId), any(LocalDateTime.class)))
              .thenReturn(Optional.empty());

      // Act: Call service method
      Optional<PumpTransaction> result = transactionService.findFirstTransactionOnDate(ptsId, nozzleId, pumpId, dateTime);

      // Assert: Verify the result is empty
      assertFalse(result.isPresent(), "Expected no transaction to be returned");
   }

   // Test for findFirstTransactionOnDateByTag
   @Test
   void testFindFirstTransactionOnDateByTag() {
      PumpTransaction expectedTransaction = new PumpTransaction();

      String ptsId = "0027003A3438510935383135";
      Long nozzleId = 1L;
      pumpId = 1L;
      String tag = "D875HK";
      LocalDateTime dateTime = LocalDateTime.now();

      when(transactionRepository.findFirstTransactionOnDateByTag(eq(ptsId), eq(nozzleId), eq(pumpId), eq(tag), any(LocalDateTime.class)))
              .thenReturn(Optional.of(expectedTransaction));

      // Act: Call service method
      Optional<PumpTransaction> result = transactionService.findFirstTransactionOnDateByTag(ptsId, nozzleId, pumpId, tag, dateTime);

      // Assert: Verify the returned transaction is as expected
      assertTrue(result.isPresent(), "Expected a transaction to be returned");
      assertSame(expectedTransaction, result.get(), "Returned transaction should match the expected one");
   }

   @Test
   void testFindFirstTransactionOnDateByTag_notFound() {
      String ptsId = "0027003A3438510935383135";
      Long nozzleId = 1L;
      pumpId = 1L;
      String tag = "D875HK";
      LocalDateTime dateTime = LocalDateTime.now();
      when(transactionRepository.findFirstTransactionOnDateByTag(eq(ptsId), eq(nozzleId), eq(pumpId), eq(tag), any(LocalDateTime.class)))
              .thenReturn(Optional.empty());

      // Act: Call service method
      Optional<PumpTransaction> result = transactionService.findFirstTransactionOnDateByTag(ptsId, nozzleId, pumpId, tag, dateTime);

      // Assert: Verify the result is empty
      assertFalse(result.isPresent(), "Expected no transaction to be returned");
   }
   @Test
   void getSalesByController_ShouldThrowException_WhenDatesAreNull() {
      assertThrows(IllegalArgumentException.class, () -> transactionService.getSalesByController(idCtr, null, null));
   }
   @Test
   void testFindPumpAttendantForTransaction_NoConfiguration() {
      when(workDayShiftPlanningService.findByStationAndDay(anyLong(), any())).thenReturn(null);

      PumpTransaction pumpTransaction = new PumpTransaction();
      pumpTransaction.setDateTime(LocalDateTime.now());

      transactionService.findPumpAttendantForTransaction(pumpTransaction, controllerPtsConfiguration);

      // Assertions can check that no pump attendant was set.
      assertNull(pumpTransaction.getPumpAttendant());
   }

   @Test
   void processUploadedTransactionPacket_TransactionAlreadyExists() {
      PumpTransaction existingTransaction = new PumpTransaction();
      existingTransaction.setTransactionReference(123L);
      existingTransaction.setState(EnumTransactionState.AUTHORIZED);
      existingTransaction.setDateTime(LocalDateTime.now());

      when(transactionRepository.findByReferenceAndPumpAndNozzleAndPtsIdAndTotalVolume(
              123L, 1L, 1L, controllerPtsConfiguration.getPtsId(), BigDecimal.valueOf(100.0)))
              .thenReturn(Optional.of(existingTransaction));

      // Act
      transactionService.processUploadedTransactionPacket(pumpUpload, controllerPtsConfiguration);

      // Verify that it updates the right fields
      verify(pumpTransactionMapper).updateAndMarkAsFinished(any(), any());
      assertEquals(EnumTransactionState.FINISHED, existingTransaction.getState());
   }
   @Test
   void processUploadedTransactionPacket_NoPumpAttendant() {
      // Mocking `pumpUpload` object
      pumpUpload = new PumpUpload();
      pumpUpload.setTransaction(12345L);

      // Initialize the Pump and Nozzle
      Pump pump = new Pump();
      pump.setIdConf(1L);
      pumpUpload.setPump(pump.getIdConf());

      Nozzle nozzle = new Nozzle();
      nozzle.setIdConf(2L);
      pumpUpload.setNozzle(nozzle.getIdConf());

      pumpUpload.setTotalVolume(100.0);
      pumpUpload.setDateTime(String.valueOf(LocalDateTime.now()));

      // Prepare a fully initialized PumpTransaction
      PumpTransaction pumpTransaction = new PumpTransaction();
      pumpTransaction.setPump(pump);
      pumpTransaction.setNozzle(nozzle);
      pumpTransaction.setDateTime(LocalDateTime.now());

      // Mock PumpAttendant
      PumpAttendant pumpAttendant = new PumpAttendant();
      pumpAttendant.setFirstName("John");
      pumpAttendant.setLastName("Doe");
      pumpTransaction.setPumpAttendant(pumpAttendant);

      // Mock FuelGrade
      FuelGrade fuelGrade = new FuelGrade();
      fuelGrade.setName("Super");
      pumpTransaction.setFuelGrade(fuelGrade);

      when(transactionRepository.save(pumpTransaction)).thenReturn(pumpTransaction);
      // Mocking the repository method to return empty (indicating no existing transaction)
      when(transactionRepository.findByReferenceAndPumpAndNozzleAndPtsIdAndTotalVolume(
              pumpUpload.getTransaction(),
              pumpUpload.getPump(),
              pumpUpload.getNozzle(),
              controllerPtsConfiguration.getPtsId(),
              BigDecimal.valueOf(pumpUpload.getTotalVolume())))
              .thenReturn(Optional.empty());

      // Mocking the mapper method to return the fully initialized PumpTransaction
      when(pumpTransactionMapper.fromPumpUploadedData(any(), any(), any(), any()))
              .thenReturn(pumpTransaction);

      // Mocking the findByTag method to return empty (no pump attendant found)
      when(pumpAttendantDao.findByTag(any(), any())).thenReturn(Optional.empty());

      // Call the method under test
      transactionService.processUploadedTransactionPacket(pumpUpload, controllerPtsConfiguration);

      // Verify that it creates the transaction once
      verify(transactionRepository, times(1)).save(any());
   }
   @Test
   void testChartOfSalesVolume_ValidDateRange() {
      LocalDateTime start = LocalDateTime.now().minusDays(10);
      LocalDateTime end = LocalDateTime.now();

      // Add necessary mocking for remote services
      when(controllerPtsConfigurationRepository.findCurrentConfigurationOnController(anyLong())).thenReturn(Optional.of(controllerPtsConfiguration));
      // Additional mocking...

      List<ChartFuelAllPumpDto> result = transactionService.chartOfSalesVolume(idCtr, "ALL", "ALL", start, end);
      assertNotNull(result);
   }
}