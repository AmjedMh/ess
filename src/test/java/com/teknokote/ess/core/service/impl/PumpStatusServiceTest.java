package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.service.cache.PumpStatusCache;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.dto.PeriodDto;
import com.teknokote.ess.dto.data.EnumPumpStatus;
import com.teknokote.ess.dto.data.FuelStatusData;
import com.teknokote.ess.dto.data.PumpStatusDto;
import com.teknokote.ess.events.listeners.PumpStatusService;
import com.teknokote.pts.client.upload.status.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.teknokote.ess.core.model.movements.EnumFilter.TODAY;
import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
@Slf4j
class PumpStatusServiceTest {

    @InjectMocks
    private PumpStatusService pumpStatusService;
    @Mock
    private PumpStatusCache pumpStatusCache;
    @Mock
    private TransactionService transactionService;
    @Mock
    private UploadStatus uploadStatus;
    @Mock
    private ControllerPtsConfiguration controllerPtsConfiguration;
    @Mock
    private Pumps pumps;

    @Mock
    private IdleStatus idleStatus;
    @Mock
    FillingStatus fillingStatus;
    @Mock
    EndOfTransactionStatus endOfTransactionStatus;

    private final String ptsId = "pts123";
    private final String configurationId = "config123";
    private LocalDateTime dateTime;
    private PeriodDto periodDto;

    @BeforeEach
     void setup() {
        MockitoAnnotations.openMocks(this);
        dateTime = LocalDateTime.now();
        periodDto = PeriodDto.builder().periodType(TODAY).startDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(06,00,00))).endDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(22,00,00))).build();
        // Setup mock behaviors
        when(controllerPtsConfiguration.getPtsId()).thenReturn(ptsId);
        when(controllerPtsConfiguration.getConfigurationId()).thenReturn(configurationId);
        when(uploadStatus.getPumps()).thenReturn(pumps);
    }
    @Test
     void testCreatePumpStatusDto() {
        // Arrange
        Long pumpId = 1L;
        EnumPumpStatus pumpStatus = EnumPumpStatus.FILLING;
        List<Long> fuelGradeIds = Arrays.asList(100L, 200L);
        when(pumpStatusCache.additionalFuelGradesCache(ptsId, pumpId, configurationId)).thenReturn(fuelGradeIds);

        PumpStatusDto pumpStatusDto = pumpStatusService.createPumpStatusDto(ptsId, pumpId, pumpStatus, configurationId);

        // Assert
        assertNotNull(pumpStatusDto);
        assertEquals(pumpId, pumpStatusDto.getPumpId());
        assertEquals(pumpStatus, pumpStatusDto.getPumpStatus());
        assertEquals(fuelGradeIds, pumpStatusDto.getAttachedFuelGrades());
    }

    @Test
    void testAddPumpStatusDtosFromIdleStatus() {
       // Arrange
       List<Long> pumpIds = Arrays.asList(1L,2L);
       List<Double> lastVolumes = Arrays.asList(10.0,20.0);
       List<Double> lastAmounts = Arrays.asList(50.0,100.0);
       List<Long> lastNozzles = Arrays.asList(101L,102L);
       List<BigDecimal> lastTotalVolumes = Arrays.asList(new BigDecimal("100.0"),new BigDecimal("120.0"));
       List<BigDecimal> lastTotalAmounts = Arrays.asList(new BigDecimal("500.0"),new BigDecimal("550.0"));
       List<Double> lastPrices = Arrays.asList(5.0,2.0);
       BigDecimal initialVolumeCashed = BigDecimal.valueOf(5);
       BigDecimal initialAmountCashed = BigDecimal.valueOf(25);
       when(idleStatus.getIds()).thenReturn(pumpIds);
       when(idleStatus.getLastVolumes()).thenReturn(lastVolumes);
       when(idleStatus.getLastAmounts()).thenReturn(lastAmounts);
       when(idleStatus.getLastNozzles()).thenReturn(lastNozzles);
       when(idleStatus.getLastTotalVolumes()).thenReturn(lastTotalVolumes);
       when(idleStatus.getLastTotalAmounts()).thenReturn(lastTotalAmounts);
       when(idleStatus.getLastPrices()).thenReturn(lastPrices);
       FuelGrade primaryFuelGrade1 = new FuelGrade();
       primaryFuelGrade1.setIdConf(1L);
       primaryFuelGrade1.setName("Gasoil");
       FuelGrade additionalFuel = new FuelGrade();
       additionalFuel.setIdConf(2L);
       additionalFuel.setName("Super");
       FuelStatusData fuelStatusData = new FuelStatusData();
       fuelStatusData.setFuelGradeId(primaryFuelGrade1.getIdConf());
       fuelStatusData.setPrice(lastPrices.get(0));
       fuelStatusData.setTotalAmount(lastTotalAmounts.get(0));
       fuelStatusData.setTotalVolume(lastTotalVolumes.get(0));
       fuelStatusData.setInitialTotalAmount(initialAmountCashed);
       fuelStatusData.setInitialTotalVolume(initialVolumeCashed);
       fuelStatusData.setAmount(lastAmounts.get(0));
       fuelStatusData.setFuelGradeName(primaryFuelGrade1.getName());
       when(pumpStatusCache.getCachedPeriodDto(ptsId,pumpIds.get(0),dateTime)).thenReturn(periodDto);
       when(pumpStatusCache.getCachedPeriodDto(ptsId,pumpIds.get(1),dateTime)).thenReturn(periodDto);
       when(pumpStatusService.getUpdatedPeriod(ptsId, pumpIds.get(0),dateTime,fuelStatusData)).thenReturn(periodDto);
       when(pumpStatusService.getUpdatedPeriod(ptsId, pumpIds.get(1),dateTime,fuelStatusData)).thenReturn(periodDto);
       when(pumpStatusCache.updateFuelGradeCache(ptsId, 1L, 101L, configurationId)).thenReturn(primaryFuelGrade1);
       when(pumpStatusCache.updateFuelGradeCache(ptsId, 2L, 102L, configurationId)).thenReturn(additionalFuel);

       when(pumpStatusCache.getAdditionalFuelGradesCache(ptsId, 2L, configurationId)).thenReturn(List.of(additionalFuel));

       List<PumpStatusDto> pumpStatusDtos = new ArrayList<>();

       pumpStatusService.addPumpStatusDtosFromIdleStatus(idleStatus, ptsId, configurationId, pumpStatusDtos, dateTime);

       // Assert
       assertEquals(2, pumpStatusDtos.size());
       assertEquals(1L, pumpStatusDtos.get(0).getPumpId());
       assertEquals(2L, pumpStatusDtos.get(1).getPumpId());
       verify(pumpStatusCache, times(2)).updateLastIdleStatusCache(eq(ptsId), anyLong(), anyList());
    }

    @Test
    void testAddPumpStatusDtosFromFillingStatus() {
        // Arrange
        List<Long> pumpIds = Arrays.asList(1L, 2L);
        List<Double> volumes = Arrays.asList(15.0, 25.0);
        List<Double> amounts = Arrays.asList(75.0, 125.0);
        List<Double> prices = Arrays.asList(5.0, 2.5);
        List<Long> fuelGradeIds = Arrays.asList(1L, 2L);
        List<String> fuelGradeNames = Arrays.asList("Gasoil", "Super");

        when(fillingStatus.getIds()).thenReturn(pumpIds);
        when(fillingStatus.getVolumes()).thenReturn(volumes);
        when(fillingStatus.getAmounts()).thenReturn(amounts);
        when(fillingStatus.getPrices()).thenReturn(prices);
        when(fillingStatus.getFuelGradeIds()).thenReturn(fuelGradeIds);
        when(fillingStatus.getFuelGradeNames()).thenReturn(fuelGradeNames);
        when(pumpStatusCache.getCachedPeriodDto(ptsId,pumpIds.get(0),dateTime)).thenReturn(periodDto);
        when(pumpStatusCache.getCachedPeriodDto(ptsId,pumpIds.get(1),dateTime)).thenReturn(periodDto);
        FuelStatusData fuelStatusData1 = new FuelStatusData();
        fuelStatusData1.setFuelGradeId(1L);
        fuelStatusData1.setFuelGradeName("Gasoil");
        fuelStatusData1.setAmount(amounts.get(0));
        fuelStatusData1.setPrice(prices.get(0));
        fuelStatusData1.setTotalAmount(BigDecimal.valueOf(50000.0));
        fuelStatusData1.setTotalVolume(BigDecimal.valueOf(20.0));
        fuelStatusData1.setInitialTotalVolume(BigDecimal.valueOf(5.0));
        fuelStatusData1.setInitialTotalAmount(BigDecimal.valueOf(12500.0));
        when(pumpStatusService.getUpdatedPeriod(ptsId, pumpIds.get(0),dateTime,fuelStatusData1)).thenReturn(periodDto);
        when(pumpStatusService.getUpdatedPeriod(ptsId, pumpIds.get(1),dateTime,fuelStatusData1)).thenReturn(periodDto);

        FuelStatusData fuelStatusData2 = new FuelStatusData();
        fuelStatusData2.setFuelGradeId(2L);
        fuelStatusData2.setFuelGradeName("Super");
        fuelStatusData2.setAmount(amounts.get(1));
        fuelStatusData2.setPrice(prices.get(1));
        fuelStatusData2.setTotalAmount(BigDecimal.valueOf(500000.0));
        fuelStatusData2.setTotalVolume(BigDecimal.valueOf(200.0));
        fuelStatusData2.setInitialTotalVolume(BigDecimal.valueOf(50.0));
        fuelStatusData2.setInitialTotalAmount(BigDecimal.valueOf(125000.0));
        when(pumpStatusCache.getLastIdleStatusCache(ptsId,pumpIds.get(0))).thenReturn(List.of(fuelStatusData1));
        when(pumpStatusCache.getLastIdleStatusCache(ptsId,pumpIds.get(1))).thenReturn(List.of(fuelStatusData2));
        List<PumpStatusDto> pumpStatusDtos = new ArrayList<>();

        pumpStatusService.addPumpStatusDtosFromFillingStatus(fillingStatus, ptsId, configurationId, pumpStatusDtos, dateTime);

        // Assert
        assertEquals(2, pumpStatusDtos.size());
        assertEquals(1L, pumpStatusDtos.get(0).getPumpId());
        assertEquals(2L, pumpStatusDtos.get(1).getPumpId());
        assertEquals("Gasoil", pumpStatusDtos.get(0).getFuelStatusData().get(0).getFuelGradeName());
        assertEquals("Super", pumpStatusDtos.get(1).getFuelStatusData().get(0).getFuelGradeName());
        assertEquals("FILLING", pumpStatusDtos.get(0).getPumpStatus().toString());
        verify(pumpStatusCache, times(2)).getLastIdleStatusCache(eq(ptsId), anyLong());
    }

    @Test
    void testAddPumpStatusDtosFromEndOfTransactionStatus() {
        // Arrange
        List<Long> pumpIds = Arrays.asList(1L, 2L);
        List<Double> volumes = Arrays.asList(15.0, 25.0);
        List<Double> amounts = Arrays.asList(75.0, 125.0);
        List<Double> prices = Arrays.asList(5.0, 2.5);
        List<Long> fuelGradeIds = Arrays.asList(1L, 2L);
        List<String> fuelGradeNames = Arrays.asList("Gasoil", "Super");

        when(endOfTransactionStatus.getIds()).thenReturn(pumpIds);
        when(endOfTransactionStatus.getVolumes()).thenReturn(volumes);
        when(endOfTransactionStatus.getAmounts()).thenReturn(amounts);
        when(endOfTransactionStatus.getPrices()).thenReturn(prices);
        when(endOfTransactionStatus.getFuelGradeIds()).thenReturn(fuelGradeIds);
        when(endOfTransactionStatus.getFuelGradeNames()).thenReturn(fuelGradeNames);
        when(pumpStatusCache.getCachedPeriodDto(ptsId,pumpIds.get(0),dateTime)).thenReturn(periodDto);
        when(pumpStatusCache.getCachedPeriodDto(ptsId,pumpIds.get(1),dateTime)).thenReturn(periodDto);
        FuelStatusData fuelStatusData1 = new FuelStatusData();
        fuelStatusData1.setFuelGradeId(1L);
        fuelStatusData1.setFuelGradeName("Gasoil");
        fuelStatusData1.setAmount(amounts.get(0));
        fuelStatusData1.setPrice(prices.get(0));
        fuelStatusData1.setTotalAmount(BigDecimal.valueOf(50000.0));
        fuelStatusData1.setTotalVolume(BigDecimal.valueOf(20.0));
        fuelStatusData1.setInitialTotalVolume(BigDecimal.valueOf(5.0));
        fuelStatusData1.setInitialTotalAmount(BigDecimal.valueOf(12500.0));
        when(pumpStatusService.getUpdatedPeriod(ptsId, pumpIds.get(0),dateTime,fuelStatusData1)).thenReturn(periodDto);
        when(pumpStatusService.getUpdatedPeriod(ptsId, pumpIds.get(1),dateTime,fuelStatusData1)).thenReturn(periodDto);

        FuelStatusData fuelStatusData2 = new FuelStatusData();
        fuelStatusData2.setFuelGradeId(2L);
        fuelStatusData2.setFuelGradeName("Super");
        fuelStatusData2.setAmount(amounts.get(1));
        fuelStatusData2.setPrice(prices.get(1));
        fuelStatusData2.setTotalAmount(BigDecimal.valueOf(500000.0));
        fuelStatusData2.setTotalVolume(BigDecimal.valueOf(200.0));
        fuelStatusData2.setInitialTotalVolume(BigDecimal.valueOf(50.0));
        fuelStatusData2.setInitialTotalAmount(BigDecimal.valueOf(125000.0));
        when(pumpStatusCache.getLastIdleStatusCache(ptsId,pumpIds.get(0))).thenReturn(List.of(fuelStatusData1));
        when(pumpStatusCache.getLastIdleStatusCache(ptsId,pumpIds.get(1))).thenReturn(List.of(fuelStatusData2));
        List<PumpStatusDto> pumpStatusDtos = new ArrayList<>();

        pumpStatusService.addPumpStatusDtosFromEndOfTransactionStatus(endOfTransactionStatus, ptsId, configurationId, pumpStatusDtos, dateTime);

        // Assert
        assertEquals(2, pumpStatusDtos.size());
        assertEquals(1L, pumpStatusDtos.get(0).getPumpId());
        assertEquals(2L, pumpStatusDtos.get(1).getPumpId());
        assertEquals("Gasoil", pumpStatusDtos.get(0).getFuelStatusData().get(0).getFuelGradeName());
        assertEquals("Super", pumpStatusDtos.get(1).getFuelStatusData().get(0).getFuelGradeName());
        assertEquals("END_OF_TRANSACTION", pumpStatusDtos.get(0).getPumpStatus().toString());
        verify(pumpStatusCache, times(2)).getLastIdleStatusCache(eq(ptsId), anyLong());
    }

}
