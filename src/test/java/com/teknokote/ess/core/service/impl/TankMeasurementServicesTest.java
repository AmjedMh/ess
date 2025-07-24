package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.model.movements.TankMeasurement;
import com.teknokote.ess.core.repository.TankRepository;
import com.teknokote.ess.core.repository.tank_measurement.TankMeasurementRepository;
import com.teknokote.ess.core.service.cache.MeasurementMapper;
import com.teknokote.ess.core.service.cache.TankMeasurementCache;
import com.teknokote.ess.dto.TankFilterDto;
import com.teknokote.ess.dto.TankMeasurementsDto;
import com.teknokote.ess.dto.charts.ReportTankMeasurementAndLevelChartDto;
import com.teknokote.ess.dto.charts.TankLevelPerSalesChartDto;
import com.teknokote.ess.dto.charts.TankMeasurementChartDto;
import com.teknokote.pts.client.enums.EnumTankStatus;
import com.teknokote.pts.client.upload.dto.MeasurementDto;
import com.teknokote.pts.client.upload.dto.UploadMeasurementDto;
import com.teknokote.pts.client.upload.dto.UploadSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TankMeasurementServicesTest {
    @Mock
    private TankRepository tankRepository;
    @Mock
    private MeasurementMapper measurementMapper;
    @Mock
    private TankDeliveryService tankDeliveryService;
    @Spy
    private TankMeasurementCache tankMeasurementCache = new TankMeasurementCache();
    @Mock
    private TankMeasurementRepository tankMeasurementRepository;
    @InjectMocks
    private TankMeasurementServices tankMeasurementService;

    private ControllerPtsConfiguration controllerPtsConfiguration;
    private UploadMeasurementDto uploadDelayedMeasurementDto;
    private UploadMeasurementDto uploadRealTimeMeasurementDto;
    private MeasurementDto realTimeMeasurement;
    private MeasurementDto previousMeasurement;
    private TankMeasurement previousDelayedTankMeasurement;
    private TankMeasurement previousRealTimeTankMeasurement;
    private MeasurementDto delayedMeasurement;
    private Tank mockTank;
    private TankMeasurement tankMeasurement;
    public static final String TEST_CTRL_PTS_ID = "003700203337510F35343338";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        Field measurementPeriod = TankMeasurementServices.class.getDeclaredField("measurementPeriod");
        measurementPeriod.setAccessible(true);
        Field field = TankMeasurementServices.class.getDeclaredField("tankMeasurementCache");
        field.setAccessible(true);
        field.set(tankMeasurementService, tankMeasurementCache);
        measurementPeriod.set(tankMeasurementService, Duration.ofMinutes(1));
        controllerPtsConfiguration = new ControllerPtsConfiguration();
        controllerPtsConfiguration.setId(1L);
        controllerPtsConfiguration.setConfigurationId("abc123");
        controllerPtsConfiguration.setPtsId(TEST_CTRL_PTS_ID);
        //last Delayed Measurement
        previousDelayedTankMeasurement = new TankMeasurement();
        previousDelayedTankMeasurement.setTank(1L);
        previousDelayedTankMeasurement.setDateTime(LocalDateTime.now().minusHours(2).minusMinutes(1));
        previousDelayedTankMeasurement.setProductVolume(90.0);
        previousDelayedTankMeasurement.setProductHeight(25.0);
        previousDelayedTankMeasurement.setWaterHeight(0.0);
        previousDelayedTankMeasurement.setTemperature(30.0);
        //last realTime Measurement
        previousRealTimeTankMeasurement = new TankMeasurement();
        previousRealTimeTankMeasurement.setTank(1L);
        previousRealTimeTankMeasurement.setDateTime(LocalDateTime.now().minusMinutes(2));
        previousRealTimeTankMeasurement.setProductVolume(95.0);
        previousRealTimeTankMeasurement.setProductHeight(25.0);
        previousRealTimeTankMeasurement.setWaterHeight(0.0);
        previousRealTimeTankMeasurement.setTemperature(30.0);
        // Mock real-time measurement
        previousMeasurement = new MeasurementDto();
        previousMeasurement.setTank(1L);
        previousMeasurement.setDateTime(LocalDateTime.now().minusHours(2).minusMinutes(2));
        previousMeasurement.setProductVolume(95.0);
        previousMeasurement.setTankStatus(EnumTankStatus.ONLINE);
        previousMeasurement.setProductHeight(25.0);
        previousMeasurement.setWaterHeight(0.0);
        previousMeasurement.setWaterVolume(0.0);
        previousMeasurement.setTemperature(30.0);
        previousMeasurement.setTankFillingPercentage(10.0);
        // Mock real-time measurement
        realTimeMeasurement = new MeasurementDto();
        realTimeMeasurement.setTank(1L);
        realTimeMeasurement.setDateTime(LocalDateTime.now());
        realTimeMeasurement.setProductVolume(100.0);
        realTimeMeasurement.setTankStatus(EnumTankStatus.ONLINE);
        realTimeMeasurement.setProductHeight(30.0);
        realTimeMeasurement.setWaterHeight(0.0);
        realTimeMeasurement.setWaterVolume(0.0);
        realTimeMeasurement.setTemperature(30.0);
        realTimeMeasurement.setTankFillingPercentage(10.0);
        // Mock delayed measurement (older than real-time)
        delayedMeasurement = new MeasurementDto();
        delayedMeasurement.setTank(1L);
        delayedMeasurement.setDateTime(LocalDateTime.now().minusHours(2).minusMinutes(1));
        delayedMeasurement.setProductVolume(80.0);
        delayedMeasurement.setTankStatus(EnumTankStatus.ONLINE);
        delayedMeasurement.setProductHeight(20.0);
        delayedMeasurement.setWaterHeight(0.0);
        delayedMeasurement.setWaterVolume(0.0);
        delayedMeasurement.setTemperature(30.0);
        delayedMeasurement.setTankFillingPercentage(10.0);

        // Mock tank entity
        mockTank = new Tank();
        mockTank.setId(1L);
        mockTank.setIdConf(1L);
        mockTank.setGrade(new FuelGrade("Gasoil",2500.00,null,null,null,null));

        uploadDelayedMeasurementDto = new UploadMeasurementDto();
        uploadDelayedMeasurementDto.setPtsId(TEST_CTRL_PTS_ID);
        uploadDelayedMeasurementDto.setConfigurationId(controllerPtsConfiguration.getConfigurationId());
        uploadDelayedMeasurementDto.setMeasurements(Map.of(1L, realTimeMeasurement));
        uploadDelayedMeasurementDto.setUploadSource(UploadSource.UploadTankMeasurement);
        uploadRealTimeMeasurementDto = new UploadMeasurementDto();
        uploadRealTimeMeasurementDto.setPtsId(TEST_CTRL_PTS_ID);
        uploadRealTimeMeasurementDto.setConfigurationId(controllerPtsConfiguration.getConfigurationId());
        uploadRealTimeMeasurementDto.setMeasurements(Map.of(1L, realTimeMeasurement));
        uploadRealTimeMeasurementDto.setUploadSource(UploadSource.UploadStatus);

        tankMeasurement = new TankMeasurement();
        tankMeasurement.setTank(1L);
        tankMeasurement.setDateTime(LocalDateTime.now());
        tankMeasurement.setProductVolume(100.0);
    }
    @Test
    void shouldProcessRealTimeMeasurement() {
        // Mock real-time measurement in cache
        when(tankMeasurementRepository.getLastMeasurement(TEST_CTRL_PTS_ID, 1L))
                .thenReturn(Optional.of(previousRealTimeTankMeasurement));
        when(measurementMapper.mapToMeasurementDto(previousRealTimeTankMeasurement))
                .thenReturn(previousMeasurement);
        lenient().when(tankMeasurementService.getLastMeasurementData(TEST_CTRL_PTS_ID, 1L)).thenReturn(previousMeasurement);
        when(tankRepository.findAllByIdConfAndControllerPtsConfiguration(anyLong(), any())).thenReturn(mockTank);

        // Execute method
        tankMeasurementService.processUploadedTankMeasurement(uploadRealTimeMeasurementDto, controllerPtsConfiguration);

        // Verify real-time update in cache
        verify(tankMeasurementCache, times(1)).updateMeasurementCache(TEST_CTRL_PTS_ID, realTimeMeasurement);

        // Verify that the tank repository was called with expected parameters
        verify(tankRepository, times(1)).findAllByIdConfAndControllerPtsConfiguration(anyLong(), any());
    }


    @Test
    void shouldProcessDelayedMeasurement() {
        when(tankMeasurementRepository.getLastMeasurementByDate(TEST_CTRL_PTS_ID, 1L,delayedMeasurement.getDateTime()))
                .thenReturn(Optional.of(previousDelayedTankMeasurement));
        // Mock delayed measurement
        lenient().when(measurementMapper.mapToMeasurementDto(previousDelayedTankMeasurement))
                .thenReturn(delayedMeasurement);
        lenient().when(tankMeasurementService.getDelayedMeasurement(TEST_CTRL_PTS_ID, 1L,delayedMeasurement.getDateTime())).thenReturn(delayedMeasurement);
        when(tankRepository.findAllByIdConfAndControllerPtsConfiguration(1L, controllerPtsConfiguration)).thenReturn(mockTank);

        // Execute method
        tankMeasurementService.processUploadedTankMeasurement(uploadDelayedMeasurementDto, controllerPtsConfiguration);

        // Verify that the cache was updated with the delayed measurement
        verify(tankMeasurementCache, times(1)).updateDelayedMeasurementCache(TEST_CTRL_PTS_ID, realTimeMeasurement);

        // Verify that the tank repository was called with expected parameters
        verify(tankRepository, times(1)).findAllByIdConfAndControllerPtsConfiguration(1L, controllerPtsConfiguration);
    }

    @Test
    void testAlignAndFillMissingData() {
        // Sample test data for TankMeasurementChartDto
        List<TankMeasurementChartDto> measurementData = new ArrayList<>();
        measurementData.add(new TankMeasurementChartDto(LocalDateTime.of(2023, 10, 1, 10, 0), 1L, 50.0));
        measurementData.add(new TankMeasurementChartDto(LocalDateTime.of(2023, 10, 1, 10, 0), 1L, 55.0));

        // Sample test data for TankLevelPerSalesChartDto
        List<TankLevelPerSalesChartDto> levelData = new ArrayList<>();
        levelData.add(new TankLevelPerSalesChartDto(1L, 45.0, LocalDateTime.of(2023, 10, 1, 10, 30)));
        levelData.add(new TankLevelPerSalesChartDto(1L, 60.0, LocalDateTime.of(2023, 10, 1, 11, 30)));

        // Invoke the method
        List<ReportTankMeasurementAndLevelChartDto> alignedData =
                tankMeasurementService.alignAndFillMissingData(measurementData, levelData);

        // Assertions
        assertNotNull(alignedData);
        assertEquals(4, alignedData.size());

        // Check the first measurement data aligned
        assertEquals("2023-10-01 10:00:00", alignedData.get(1).getDateTime());
        assertEquals(1L, alignedData.get(0).getTank());
        assertEquals(50.0, alignedData.get(0).getMeasurementVolume());
        assertNull(alignedData.get(0).getLevelVolume());

        // Check the first level data aligned
        assertEquals("2023-10-01 10:00:00", alignedData.get(1).getDateTime());
        assertEquals(1L, alignedData.get(1).getTank());
        assertNull(alignedData.get(1).getLevelVolume());

        // Check the second measurement data aligned
        assertEquals("2023-10-01 10:30:00", alignedData.get(2).getDateTime());
        assertEquals(1L, alignedData.get(2).getTank());
        assertEquals(55.0, alignedData.get(2).getMeasurementVolume());
        assertEquals(45.0, alignedData.get(2).getLevelVolume());

        // Check the second level data aligned
        assertEquals("2023-10-01 11:30:00", alignedData.get(3).getDateTime());
        assertEquals(1L, alignedData.get(3).getTank());
        assertEquals(55.0, alignedData.get(3).getMeasurementVolume());
        assertEquals(60.0, alignedData.get(3).getLevelVolume());
    }

    @Test
    void testAlignAndFillMissingData_EmptyInputs() {
        // Test when both input lists are empty
        List<ReportTankMeasurementAndLevelChartDto> alignedData =
                tankMeasurementService.alignAndFillMissingData(new ArrayList<>(), new ArrayList<>());

        // Assertions
        assertNotNull(alignedData);
        assertTrue(alignedData.isEmpty());
    }

    @Test
    void testAlignAndFillMissingData_OnlyMeasurementData() {
        // Sample test data for TankMeasurementChartDto
        List<TankMeasurementChartDto> measurementData = new ArrayList<>();
        measurementData.add(new TankMeasurementChartDto(LocalDateTime.of(2023, 10, 1, 10, 0), 1L, 50.0));

        // No level data is provided
        List<TankLevelPerSalesChartDto> levelData = new ArrayList<>();

        // Invoke the method
        List<ReportTankMeasurementAndLevelChartDto> alignedData =
                tankMeasurementService.alignAndFillMissingData(measurementData, levelData);

        // Assertions
        assertNotNull(alignedData);
        assertEquals(1, alignedData.size());

        // Validate the measurement data
        assertEquals("2023-10-01 10:00:00", alignedData.get(0).getDateTime());
        assertEquals(1L, alignedData.get(0).getTank());
        assertEquals(50.0, alignedData.get(0).getMeasurementVolume());
        assertNull(alignedData.get(0).getLevelVolume());
    }

    @Test
    void testAlignAndFillMissingData_OnlyLevelData() {
        // Sample test data for TankLevelPerSalesChartDto
        List<TankLevelPerSalesChartDto> levelData = new ArrayList<>();
        levelData.add(new TankLevelPerSalesChartDto(1L, 50.0, LocalDateTime.of(2023, 10, 1, 10, 0)));

        // No measurement data is provided
        List<TankMeasurementChartDto> measurementData = new ArrayList<>();

        // Invoke the method
        List<ReportTankMeasurementAndLevelChartDto> alignedData =
                tankMeasurementService.alignAndFillMissingData(measurementData, levelData);

        // Assertions
        assertNotNull(alignedData);
        assertEquals(1, alignedData.size());

        // Validate the level data
        assertNull(alignedData.get(0).getTank());
        assertNull(alignedData.get(0).getMeasurementVolume());
        assertEquals(50.0, alignedData.get(0).getLevelVolume());
        assertEquals("2023-10-01 10:00:00", alignedData.get(0).getDateTime());
    }

    @Test
    void testSave() {
        // Arrange
        when(tankMeasurementRepository.save(any(TankMeasurement.class))).thenReturn(tankMeasurement);

        // Act
        TankMeasurement result = tankMeasurementService.save(tankMeasurement);

        // Assert
        assertNotNull(result);
        assertEquals(tankMeasurement.getTank(), result.getTank());
        verify(tankMeasurementRepository, times(1)).save(tankMeasurement);
    }

    @Test
    void testGetAllTanksMeasurements() {
        // Arrange
        String ptsId = "test_pts_id";
        Map<Long, MeasurementDto> cachedMeasurements = new HashMap<>();
        MeasurementDto measurementDto = new MeasurementDto();
        cachedMeasurements.put(1L, measurementDto);

        when(tankMeasurementCache.getTankMeasurementCache(ptsId)).thenReturn(cachedMeasurements);

        // Act
        Map<Long, MeasurementDto> result = tankMeasurementService.getAllTanksMeasurements(ptsId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(measurementDto, result.get(1L));
        verify(tankMeasurementCache, times(1)).getTankMeasurementCache(ptsId);
    }

    @Test
    void testGetAllTankMeasurementsSortedByTank() {
        // Arrange
        String ptsId = "test_pts_id";
        Map<Long, MeasurementDto> cachedMeasurements = new HashMap<>();

        MeasurementDto measurement1 = new MeasurementDto();
        measurement1.setTank(2L); // Two measurements with different tank IDs
        cachedMeasurements.put(2L, measurement1);

        MeasurementDto measurement2 = new MeasurementDto();
        measurement2.setTank(1L);
        cachedMeasurements.put(1L, measurement2);

        when(tankMeasurementCache.getTankMeasurementCache(ptsId)).thenReturn(cachedMeasurements);

        // Act
        List<MeasurementDto> result = tankMeasurementService.getAllTankMeasurementsSortedByTank(ptsId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getTank()); // First should be tank 1
        assertEquals(2L, result.get(1).getTank()); // Second should be tank 2
        verify(tankMeasurementCache, times(1)).getTankMeasurementCache(ptsId);
    }

    @Test
    void testFindById() {
        // Arrange
        when(tankMeasurementRepository.findById(anyLong())).thenReturn(Optional.of(tankMeasurement));

        // Act
        Optional<TankMeasurement> result = tankMeasurementService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(tankMeasurement, result.get());
        verify(tankMeasurementRepository, times(1)).findById(1L);
    }

    @Test
    void testFindMeasurementByFilter() {
        // Arrange
        Long idCtr = 1L;
        TankFilterDto filterDto = new TankFilterDto();
        int page = 0;
        int size = 10;
        Page<TankMeasurement> pageMock = mock(Page.class);

        when(tankMeasurementRepository.findMeasurementByFilterAndIdController(idCtr, filterDto, page, size)).thenReturn(pageMock);

        // Act
        Page<TankMeasurement> result = tankMeasurementService.findMeasurementByFilter(idCtr, filterDto, page, size);

        // Assert
        assertNotNull(result);
        verify(tankMeasurementRepository, times(1)).findMeasurementByFilterAndIdController(idCtr, filterDto, page, size);
    }

    @Test
    void testReportTankMeasurementsByTankChart() {
        // Arrange
        Long idCtr = 1L;
        String tankId = "1";
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        List<TankMeasurementChartDto> chartData = new ArrayList<>();
        when(tankMeasurementRepository.findAllByControllerPtsIdAndTankIdAndPeriod(idCtr, Long.valueOf(tankId), startDate, endDate)).thenReturn(chartData);

        // Act
        List<TankMeasurementChartDto> result = tankMeasurementService.reportTankMeasurementsByTankChart(idCtr, tankId, startDate, endDate);

        // Assert
        assertNotNull(result);
        verify(tankMeasurementRepository, times(1)).findAllByControllerPtsIdAndTankIdAndPeriod(idCtr, Long.valueOf(tankId), startDate, endDate);
    }

    @Test
    void testShouldSkipMeasurement() {
        // Arrange
        MeasurementDto lastMeasurement = new MeasurementDto();
        lastMeasurement.setProductVolume(100.0);
        lastMeasurement.setTankStatus(EnumTankStatus.ONLINE);
        lastMeasurement.setDateTime(LocalDateTime.now().minusMinutes(5)); // Last measurement time

        MeasurementDto receivedMeasurement = new MeasurementDto();
        receivedMeasurement.setProductVolume(100.0);
        receivedMeasurement.setTankStatus(EnumTankStatus.ONLINE);
        receivedMeasurement.setDateTime(LocalDateTime.now()); // Current time

        // Act
        boolean result = tankMeasurementService.shouldSkipMeasurement(lastMeasurement, receivedMeasurement);

        // Assert
        assertFalse(result); // Should not skip since time difference is > 1 minute.
    }


    @Test
    void testMapTankMeasurementToDto() {
        // Arrange
        when(measurementMapper.toDto(any(TankMeasurement.class))).thenReturn(TankMeasurementsDto.builder().build());

        // Act
        TankMeasurementsDto dto = tankMeasurementService.mapTankMeasurementToDto(tankMeasurement);

        // Assert
        assertNotNull(dto);
        verify(measurementMapper, times(1)).toDto(tankMeasurement);
    }

    @Test
    void testMapToMeasurementDto() {
        // Arrange
        when(measurementMapper.mapToMeasurementDto(any(TankMeasurement.class))).thenReturn(new MeasurementDto());

        // Act
        MeasurementDto dto = tankMeasurementService.mapToMeasurementDto(tankMeasurement);

        // Assert
        assertNotNull(dto);
        verify(measurementMapper, times(1)).mapToMeasurementDto(tankMeasurement);
    }
}

