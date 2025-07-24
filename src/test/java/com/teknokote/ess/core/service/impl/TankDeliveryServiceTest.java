package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.EnumDeliveryStatus;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.model.movements.TankDelivery;
import com.teknokote.ess.core.model.movements.TankLevelPerSales;
import com.teknokote.ess.core.repository.tank_delivery.TankDeliveryRepository;
import com.teknokote.ess.core.repository.transactions.TransactionRepository;
import com.teknokote.ess.core.service.cache.MeasurementTracking;
import com.teknokote.ess.core.service.cache.TankDeliveryMapper;
import com.teknokote.ess.core.service.impl.transactions.TankLevelPerSalesService;
import com.teknokote.ess.dto.TankDeliveryDto;
import com.teknokote.ess.dto.TankFilterDto;
import com.teknokote.pts.client.enums.EnumTankStatus;
import com.teknokote.pts.client.upload.dto.MeasurementDto;
import com.teknokote.pts.client.upload.dto.UploadSource;
import com.teknokote.pts.client.upload.status.Measurement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TankDeliveryServiceTest {
   @InjectMocks
   private TankDeliveryService tankDeliveryService;
   @Mock
   private TankDeliveryRepository tankDeliveryRepository;
   @Mock
   private TankLevelPerSalesService tankLevelPerSalesService;
   @Mock
   private TankDeliveryMapper tankDeliveryMapper;
   @Mock
   private ControllerPtsConfiguration controllerPtsConfiguration;
   @Mock
   private TransactionRepository transactionRepository;
   @BeforeEach
   void setUp() throws Exception {
      Field volumeFluctuationField = TankDeliveryService.class.getDeclaredField("volumeFluctuation");
      volumeFluctuationField.setAccessible(true);
      volumeFluctuationField.set(tankDeliveryService, 5.0);
   }

   @Test
   void addTankDelivery()
   {
      Duration dureeDepotage = Duration.between(LocalDateTime.of(2024,10,17,11,00,00), LocalDateTime.of(2024,10,17,12,10,00));
      long hours = dureeDepotage.toHours();
      long minutes = dureeDepotage.toMinutes() % 60;
      long seconds = dureeDepotage.getSeconds() % 60;

      String formattedDuration = String.format("%02dh%02dmn%02ds", hours, minutes, seconds);

      assertEquals("01h10mn00s",formattedDuration);
   }
   @Test
   void testMonitorDepotage_NoPreviousVolume() {
      // Arrange
      String ptsId = "0027003A3438510935383135";
      Tank tank = new Tank();
      tank.setIdConf(1L);
      MeasurementDto newMeasurement = new MeasurementDto();
      newMeasurement.setProductVolume(100.0);

      MeasurementDto previousMeasurement = new MeasurementDto();
      previousMeasurement.setProductVolume(null); // No previous volume available

      // Act
      ControllerPtsConfiguration controllerConfiguration = new ControllerPtsConfiguration();
      controllerConfiguration.setPtsId(ptsId);
      tankDeliveryService.monitorDepotage(tank, newMeasurement, previousMeasurement, controllerPtsConfiguration,UploadSource.UploadStatus);
      // Assert
      // Ensure no interactions with the repository when previous volume is null
      verifyNoInteractions(tankDeliveryRepository, tankLevelPerSalesService);
   }

   @Test
   void testMonitorDepotage_VolumeIncrease_NewDeliveryCreated() {
      // Arrange
      String ptsId = "0027003A3438510935383135";
      Tank tank = new Tank();
      tank.setIdConf(1L);
      FuelGrade fuelGrade = new FuelGrade();
      fuelGrade.setName("Gasoil");
      fuelGrade.setIdConf(1L);
      tank.setGrade(fuelGrade);
      ControllerPts controllerPts = new ControllerPts();
      controllerPts.setId(1L);
      controllerPts.setPtsId(ptsId);
      ControllerPtsConfiguration controllerConfiguration = new ControllerPtsConfiguration();
      controllerConfiguration.setId(1L);
      controllerConfiguration.setPtsId(ptsId);
      controllerConfiguration.setConfigurationId("90f3714b");
      controllerConfiguration.setControllerPts(controllerPts);
      // First measurement
      MeasurementDto firstMeasurement = new MeasurementDto();
      firstMeasurement.setProductVolume(100.0);
      firstMeasurement.setProductHeight(50.0);
      firstMeasurement.setDateTime(LocalDateTime.now());
      firstMeasurement.setTankStatus(EnumTankStatus.ONLINE);


      // Second measurement
      MeasurementDto secondMeasurement = new MeasurementDto();
      secondMeasurement.setProductVolume(150.0);  // Volume increased
      secondMeasurement.setProductHeight(75.0);
      secondMeasurement.setDateTime(LocalDateTime.now().plusMinutes(1));
      secondMeasurement.setTankStatus(EnumTankStatus.ONLINE);

      // Third measurement
      MeasurementDto thirdMeasurement = new MeasurementDto();
      thirdMeasurement.setProductVolume(200.0);  // Volume increased again
      thirdMeasurement.setProductHeight(100.0);
      thirdMeasurement.setWaterHeight(0.0);
      thirdMeasurement.setTemperature(31.0);
      thirdMeasurement.setDateTime(LocalDateTime.now().plusMinutes(2));
      thirdMeasurement.setTankStatus(EnumTankStatus.ONLINE);


      // Mock previous measurement (for calculating volume fluctuation)
      MeasurementDto previousMeasurementStat = new MeasurementDto();
      previousMeasurementStat.setProductVolume(100.0);
      previousMeasurementStat.setProductHeight(50.0);
      previousMeasurementStat.setDateTime(LocalDateTime.now().minusMinutes(1));
      previousMeasurementStat.setTankStatus(EnumTankStatus.ONLINE);

      Measurement previousMeasurement = new Measurement();
      previousMeasurement.setProductVolume(100.0);
      previousMeasurement.setProductHeight(50.0);
      previousMeasurement.setDateTime(LocalDateTime.now().minusMinutes(1));


      MeasurementTracking measurementTracking = new MeasurementTracking();
      // Adding measurements so that stableOrIncreasingCount becomes 3
      measurementTracking.updateMeasurementTracking(firstMeasurement, previousMeasurement.getProductVolume(),5.0);
      measurementTracking.updateMeasurementTracking(secondMeasurement, firstMeasurement.getProductVolume(),5.0);
      measurementTracking.updateMeasurementTracking(thirdMeasurement, secondMeasurement.getProductVolume(),5.0);
      // Mock repository method (for finding previous deliveries)
      when(tankDeliveryRepository.findLastDeliveryByTankIdConfAndPtsId(ptsId, tank.getIdConf(),thirdMeasurement.getDateTime()))
              .thenReturn(Optional.empty());  // No previous delivery
      // Mock the tankDeliveryMapper
      TankDelivery mockDelivery = new TankDelivery();
      mockDelivery.setProductVolume(100.0);
      mockDelivery.setTank(tank);
      mockDelivery.setWaterHeight(0.0);
      mockDelivery.setStatus(EnumDeliveryStatus.IN_PROGRESS);
      mockDelivery.setProductHeight(BigDecimal.valueOf(50));
      mockDelivery.setStartDateTime(previousMeasurement.getDateTime());
      mockDelivery.setEndDateTime(thirdMeasurement.getDateTime());
      mockDelivery.setTemperature(BigDecimal.valueOf(31.0));
      when(tankDeliveryMapper.measurementToDelivery(thirdMeasurement, previousMeasurementStat, tank, controllerConfiguration, thirdMeasurement.getDateTime(), EnumDeliveryStatus.IN_PROGRESS))
              .thenReturn(mockDelivery);
      when(tankDeliveryRepository.save(any(TankDelivery.class))).thenAnswer(invocation -> invocation.getArgument(0));
      // Act
      tankDeliveryService.monitorDepotage(tank, thirdMeasurement, previousMeasurementStat, controllerConfiguration,UploadSource.UploadStatus);

      // Assert
      verify(tankDeliveryRepository, times(1)).save(any(TankDelivery.class));
      verify(tankLevelPerSalesService, times(1)).save(any(TankLevelPerSales.class));
   }

   @Test
   void testMonitorDepotage_VolumeDecrease_DeliveryFinalized() {
      // Arrange
      String ptsId = "0027003A3438510935383135";
      Tank tank = new Tank();
      tank.setIdConf(1L);
      FuelGrade fuelGrade = new FuelGrade();
      fuelGrade.setName("Gasoil");
      fuelGrade.setIdConf(1L);
      tank.setGrade(fuelGrade);
      ControllerPts controllerPts = new ControllerPts();
      controllerPts.setId(1L);
      controllerPts.setPtsId(ptsId);
      ControllerPtsConfiguration controllerConfiguration = new ControllerPtsConfiguration();
      controllerConfiguration.setId(1L);
      controllerConfiguration.setPtsId(ptsId);
      controllerConfiguration.setConfigurationId("90f3714b");
      controllerConfiguration.setControllerPts(controllerPts);
      MeasurementDto previousMeasurementStat = new MeasurementDto();
      previousMeasurementStat.setProductVolume(500.0);
      previousMeasurementStat.setProductHeight(190.0);
      previousMeasurementStat.setDateTime(LocalDateTime.now().minusMinutes(1));
      previousMeasurementStat.setTankStatus(EnumTankStatus.ONLINE);

      MeasurementDto previousMeasurement = new MeasurementDto();
      previousMeasurement.setProductVolume(500.0);
      previousMeasurement.setProductHeight(190.0);
      previousMeasurement.setDateTime(LocalDateTime.now().minusMinutes(1));
      previousMeasurement.setTankStatus(EnumTankStatus.ONLINE);

      MeasurementDto firstMeasurement = new MeasurementDto();
      firstMeasurement.setProductVolume(520.0);
      firstMeasurement.setProductHeight(200.0);
      firstMeasurement.setDateTime(LocalDateTime.now());
      firstMeasurement.setTankStatus(EnumTankStatus.ONLINE);


      // Second measurement
      MeasurementDto secondMeasurement = new MeasurementDto();
      secondMeasurement.setProductVolume(490.0);  // Volume decreased
      secondMeasurement.setProductHeight(190.0);
      secondMeasurement.setDateTime(LocalDateTime.now().plusMinutes(1));
      secondMeasurement.setTankStatus(EnumTankStatus.ONLINE);


      // Third measurement
      MeasurementDto thirdMeasurement = new MeasurementDto();
      thirdMeasurement.setProductVolume(488.0);
      thirdMeasurement.setProductHeight(188.0);
      thirdMeasurement.setDateTime(LocalDateTime.now().plusMinutes(2));
      thirdMeasurement.setTankStatus(EnumTankStatus.ONLINE);

      //last measurement
      MeasurementDto lastMeasurement = new MeasurementDto();
      lastMeasurement.setProductVolume(486.0);
      lastMeasurement.setProductHeight(188.0);
      lastMeasurement.setDateTime(LocalDateTime.now().plusMinutes(2));
      lastMeasurement.setTankStatus(EnumTankStatus.ONLINE);


      TankDelivery ongoingDelivery = new TankDelivery();
      ongoingDelivery.setStatus(EnumDeliveryStatus.IN_PROGRESS);
      ongoingDelivery.setUploadSource(UploadSource.UploadStatus);
      ongoingDelivery.setStartProductVolume(100.0);
      ongoingDelivery.setStartProductHeight(BigDecimal.valueOf(50));
      ongoingDelivery.setStartDateTime(LocalDateTime.now().minusMinutes(15));
      ongoingDelivery.setTank(tank);
      ongoingDelivery.setTemperature(BigDecimal.valueOf(31.0));
      MeasurementTracking measurementTracking = new MeasurementTracking();
      // Adding measurements so that stableOrDecreased becomes 4
      measurementTracking.updateMeasurementTracking(firstMeasurement, previousMeasurementStat.getProductVolume(),5.0);
      measurementTracking.updateMeasurementTracking(secondMeasurement, firstMeasurement.getProductVolume(),5.0);
      measurementTracking.updateMeasurementTracking(thirdMeasurement, secondMeasurement.getProductVolume(),5.0);
      measurementTracking.updateMeasurementTracking(lastMeasurement,thirdMeasurement.getProductVolume(),5.0);
      when(tankDeliveryRepository.findLastDeliveryByTankIdConfAndPtsId(ptsId, tank.getIdConf(),lastMeasurement.getDateTime()))
              .thenReturn(Optional.of(ongoingDelivery));
      when(transactionRepository.findSalesVolumeByDateAndTank(eq(ptsId), eq(tank.getIdConf()), any(LocalDateTime.class), any(LocalDateTime.class)))
              .thenReturn(10.0);
      when(tankDeliveryRepository.save(any(TankDelivery.class))).thenAnswer(invocation -> invocation.getArgument(0));
      // Act
      tankDeliveryService.monitorDepotage(tank, lastMeasurement, previousMeasurementStat, controllerConfiguration,UploadSource.UploadStatus);

      // Assert
      verify(tankDeliveryRepository, times(1)).save(any(TankDelivery.class));
      verify(tankLevelPerSalesService, times(1)).save(any(TankLevelPerSales.class));
   }
   @Test
   void testGetAll_ShouldReturnListOfTankDeliveries() {
      // Arrange
      TankDelivery delivery1 = new TankDelivery();
      TankDelivery delivery2 = new TankDelivery();
      List<TankDelivery> expectedDeliveries = List.of(delivery1, delivery2);
      when(tankDeliveryRepository.findAll()).thenReturn(expectedDeliveries);

      // Act
      List<TankDelivery> result = tankDeliveryService.getAll();

      // Assert
      assertEquals(expectedDeliveries, result);
   }
   @Test
   void testFindDeliveryByFilter_ShouldReturnFilteredPage() {
      // Arrange
      Long controllerId = 1L;
      TankFilterDto filterDto = new TankFilterDto();
      Page<TankDelivery> expectedPage = mock(Page.class);
      when(tankDeliveryRepository.findTankDeliveryByIdController(controllerId, filterDto, 0, 10)).thenReturn(expectedPage);

      // Act
      Page<TankDelivery> result = tankDeliveryService.findDeliveryByFilter(controllerId, filterDto, 0, 10);

      // Assert
      assertEquals(expectedPage, result);
   }
   @Test
   void testFindLatestTankDeliveryByTankId_ShouldReturnLatestDelivery() {
      // Arrange
      Long controllerId = 1L;
      Long tankId = 2L;
      TankDelivery expectedDelivery = new TankDelivery();
      when(tankDeliveryRepository.findLastDeliveriesByTankIdConf(controllerId, tankId)).thenReturn(expectedDelivery);

      // Act
      TankDelivery result = tankDeliveryService.findLatestTankDeliveryByTankId(controllerId, tankId);

      // Assert
      assertEquals(expectedDelivery, result);
   }
   @Test
   void testMapDeliveryToDto_ShouldReturnMappedDto() {
      // Arrange
      TankDelivery delivery = new TankDelivery();
      TankDeliveryDto expectedDto = new TankDeliveryDto();
      when(tankDeliveryMapper.toDto(delivery)).thenReturn(expectedDto);

      // Act
      TankDeliveryDto result = tankDeliveryService.mapDeliveryToDto(delivery);

      // Assert
      assertEquals(expectedDto, result);
   }
   @Test
   void testGetLastDelivery() {
      // Arrange
      String ptsId = "0027003A3438510935383135";
      Long tankId = 1L;
      LocalDateTime dateTime = LocalDateTime.now();
      TankDelivery delivery = new TankDelivery();
      when(tankDeliveryRepository.findLastDeliveryByTankIdConfAndPtsId(ptsId, tankId, dateTime)).thenReturn(Optional.of(delivery));

      // Act
      TankDelivery result = tankDeliveryService.getLastDelivery(ptsId, tankId, dateTime);

      // Assert
      assertEquals(delivery, result);
   }
   @Test
   void testUpdateTankDelivery() {
      // Arrange
      FuelGrade fuelGrade = new FuelGrade();
      fuelGrade.setIdConf(1L);
      fuelGrade.setName("Gasoil");

      Tank tank = new Tank();
      tank.setIdConf(1L);
      tank.setGrade(fuelGrade);

      TankDelivery delivery = new TankDelivery();
      LocalDateTime startDateTime = LocalDateTime.now().minusHours(1);
      LocalDateTime endDateTime = LocalDateTime.now();
      delivery.setStartDateTime(startDateTime);
      delivery.setEndDateTime(endDateTime);
      delivery.setTank(tank);

      // Initialize BigDecimal fields
      delivery.setStartProductHeight(BigDecimal.valueOf(50.0));
      delivery.setStartProductVolume(100.0);

      MeasurementDto measurement = new MeasurementDto();
      measurement.setProductHeight(75.0);
      measurement.setProductVolume(150.0);

      ControllerPtsConfiguration controllerConfiguration = new ControllerPtsConfiguration();

      // Set up mock behavior for repository
      when(tankDeliveryRepository.save(any(TankDelivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      TankDelivery result = tankDeliveryService.updateTankDelivery(delivery, measurement, controllerConfiguration, endDateTime, UploadSource.UploadStatus);

      // Assert
      assertEquals(delivery, result);
   }

   @Test
   void testCreateNewTankLevelPerSales() {
      // Arrange
      TankDelivery delivery = new TankDelivery();

      // Arrange
      FuelGrade fuelGrade = new FuelGrade();
      fuelGrade.setIdConf(1L);
      fuelGrade.setName("Gasoil");

      Tank tank = new Tank();
      tank.setIdConf(1L);
      tank.setGrade(fuelGrade);
      delivery.setTank(tank);

      ControllerPtsConfiguration controllerConfiguration = new ControllerPtsConfiguration();
      // Act
      tankDeliveryService.createNewTankLevelPerSales(delivery, controllerConfiguration);

      // Assert
      verify(tankLevelPerSalesService).save(any(TankLevelPerSales.class));
   }
}
