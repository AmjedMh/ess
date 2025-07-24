package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.repository.TankRepository;
import com.teknokote.ess.core.service.impl.transactions.TankLevelPerSalesService;
import com.teknokote.ess.dto.TankConfigDto;
import com.teknokote.ess.dto.charts.TankLevelPerSalesChartDto;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSTank;
import com.teknokote.pts.client.response.configuration.PTSTankConfigurationResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSTankConfigurationResponsePacketData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TankServiceTest {

    @InjectMocks
    private TankService tankService;
    @Mock
    private TankRepository tankRepository;
    @Mock
    private TankLevelPerSalesService tankLevelPerSalesService;
    @Mock
    private FuelGradesServiceImpl fuelGradesService;
    @Mock
    private ControllerPtsConfigurationService controllerPtsConfigurationService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveTank() {
        Tank tank = new Tank();
        tank.setHeight(100L);
        tank.setHighProductAlarm(90L);

        when(tankRepository.save(any(Tank.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tank savedTank = tankService.save(tank);

        assertNotNull(savedTank);
        assertEquals(100L, savedTank.getHeight());
        assertEquals(90L, savedTank.getHighProductAlarm());

        verify(tankRepository, times(1)).save(any(Tank.class));
    }

    @Test
    void testUpdateTank() {
        Long tankId = 1L;
        Tank existingTank = new Tank();
        existingTank.setId(tankId);

        Tank updatedTank = new Tank();
        updatedTank.setHeight(150L);

        when(tankRepository.findById(tankId)).thenReturn(Optional.of(existingTank));
        when(tankRepository.save(any(Tank.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tankService.update(tankId, updatedTank);

        assertEquals(150L, existingTank.getHeight());
        verify(tankRepository, times(1)).save(any(Tank.class));
    }

    @Test
    void testFindByIdConfAndControllerPtsConfiguration() {
        Long idConf = 1L;
        ControllerPtsConfiguration configuration = new ControllerPtsConfiguration();
        Tank expectedTank = new Tank();

        when(tankRepository.findAllByIdConfAndControllerPtsConfiguration(idConf, configuration))
                .thenReturn(expectedTank);

        Tank result = tankService.findAllByIdConfAndControllerPtsConfiguration(idConf, configuration);

        assertNotNull(result);
        assertEquals(expectedTank, result);

        verify(tankRepository, times(1)).findAllByIdConfAndControllerPtsConfiguration(idConf, configuration);
    }

    @Test
    void testFindByIdConfAndControllerPtsConfigurationAndPtsId() {
        Long idConf = 1L;
        String ptsId = "PTS123";
        ControllerPtsConfiguration configuration = new ControllerPtsConfiguration();
        Tank expectedTank = new Tank();

        when(tankRepository.findByIdConfAndControllerPtsConfigurationAndPtsId(configuration, ptsId, idConf))
                .thenReturn(expectedTank);

        Tank result = tankService.findByIdConfAndControllerPtsConfigurationAndPtsId(configuration, ptsId, idConf);

        assertNotNull(result);
        assertEquals(expectedTank, result);

        verify(tankRepository, times(1))
                .findByIdConfAndControllerPtsConfigurationAndPtsId(configuration, ptsId, idConf);
    }

    @Test
    void testUpdateNonExistingTank() {
        Long id = 999L;
        Tank updatedTank = new Tank();

        when(tankRepository.findById(id)).thenReturn(Optional.empty());

        tankService.update(id, updatedTank);

        verify(tankRepository, never()).save(any(Tank.class));
    }

    @Test
    void testAddNewTank() {
        ControllerPtsConfiguration controllerPtsConfiguration = new ControllerPtsConfiguration();
        JsonPTSResponse jsonResponse = new JsonPTSResponse();
        jsonResponse.setPackets(new ArrayList<>());

        PTSTankConfigurationResponsePacket responsePacket = mock(PTSTankConfigurationResponsePacket.class);
        PTSTankConfigurationResponsePacketData data = mock(PTSTankConfigurationResponsePacketData.class);
        PTSTank ptsTank = mock(PTSTank.class);

        // Mocking the response structure
        jsonResponse.getPackets().add(responsePacket);
        when(responsePacket.getData()).thenReturn(data);
        when(data.getTanks()).thenReturn(Collections.singletonList(ptsTank));

        tankService.addNewTank(controllerPtsConfiguration, jsonResponse);

        // Verify interactions (ensure that addTank was called)
        verify(tankRepository, times(1)).save(any(Tank.class));
    }

    @Test
    void testAddTank() {
        PTSTank inputTank = mock(PTSTank.class);
        ControllerPtsConfiguration controllerPtsConfig = new ControllerPtsConfiguration();
        Tank expectedTank = new Tank();

        when(tankRepository.findAllByIdConfAndControllerPtsConfiguration(anyLong(), any())).thenReturn(null);
        when(tankRepository.save(any(Tank.class))).thenReturn(expectedTank);

        Tank resultTank = tankService.addTank(inputTank, controllerPtsConfig);

        assertNotNull(resultTank);
        verify(tankRepository, times(1)).save(any(Tank.class));
    }

    @Test
    void testTankLevelChangesChart() {
        Long controllerId = 1L;
        String tankId = "1";
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        TankLevelPerSalesChartDto chartDto = new TankLevelPerSalesChartDto(1L,50.0,LocalDateTime.now());
        List<TankLevelPerSalesChartDto> expectedList = Collections.singletonList(chartDto);

        when(tankLevelPerSalesService.findAllByControllerPtsIdAndTankPeriod(controllerId, tankId, startDate, endDate))
                .thenReturn(expectedList);

        List<TankLevelPerSalesChartDto> result = tankService.tankLevelChangesChart(controllerId, tankId, startDate, endDate);

        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
        assertEquals(expectedList.get(0), result.get(0));
    }
    @Test
    void testMapToTankConfigDto_ValidTank() {
        // Given
        Tank tank = new Tank();
        tank.setIdConf(1L);
        FuelGrade fuelGrade = new FuelGrade();
        fuelGrade.setIdConf(2L);
        fuelGrade.setName("Diesel");
        tank.setGrade(fuelGrade);
        tank.setHeight(100L);
        tank.setHighProductAlarm(90L);
        tank.setHighWaterAlarmHeight(80L);
        tank.setCriticalHighProductAlarm(70L);
        tank.setCriticalLowProductAlarm(60L);
        tank.setLowProductAlarmHeight(50L);

        // When
        TankConfigDto resultDto = tankService.mapToTankConfigDto(tank);

        // Then
        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getIdConf());
        assertEquals(2L, resultDto.getFuelGradeId());
        assertEquals(100L, resultDto.getHeight());
        assertEquals("Diesel", resultDto.getFuelGrade());
        assertEquals(90L, resultDto.getHighProductAlarm());
        assertEquals(80L, resultDto.getHighWaterAlarm());
        assertEquals(70L, resultDto.getCriticalHighProductAlarm());
        assertEquals(60L, resultDto.getCriticalLowProductAlarm());
        assertEquals(50L, resultDto.getLowProductAlarm());
    }

    @Test
    void testMapToTankConfigDto_TankWithoutFuelGrade() {
        // Given
        FuelGrade fuelGrade = new FuelGrade();

        Tank tank = new Tank();
        tank.setIdConf(1L);
        tank.setGrade(fuelGrade);
        tank.setHeight(100L);

        // When
        TankConfigDto resultDto = tankService.mapToTankConfigDto(tank);

        // Then
        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getIdConf());
        assertNull(resultDto.getFuelGradeId());
        assertEquals(100L, resultDto.getHeight());
        assertNull(resultDto.getFuelGrade());
    }

    @Test
    void testFindTankByControllerOnCurrentConfiguration() {
        // Given
        Long controllerId = 1L;
        ControllerPtsConfiguration config = new ControllerPtsConfiguration();
        List<Tank> expectedTanks = new ArrayList<>();
        Tank tank1 = new Tank();
        Tank tank2 = new Tank();
        expectedTanks.add(tank1);
        expectedTanks.add(tank2);

        when(controllerPtsConfigurationService.findCurrentConfigurationOnController(controllerId)).thenReturn(config);
        when(tankRepository.findTankByControllerConfiguration(config)).thenReturn(expectedTanks);

        // When
        List<Tank> result = tankService.findTankByControllerOnCurrentConfiguration(controllerId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedTanks, result);
    }

    @Test
    void testFindTankByControllerOnCurrentConfiguration_NoConfiguration() {
        // Given
        Long controllerId = 1L;

        when(controllerPtsConfigurationService.findCurrentConfigurationOnController(controllerId)).thenReturn(null);
        when(tankRepository.findTankByControllerConfiguration(null)).thenReturn(Collections.emptyList());

        // When
        List<Tank> result = tankService.findTankByControllerOnCurrentConfiguration(controllerId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}

