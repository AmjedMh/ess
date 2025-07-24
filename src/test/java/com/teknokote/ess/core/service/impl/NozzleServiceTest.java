package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.ControllerDao;
import com.teknokote.ess.core.model.configuration.*;
import com.teknokote.ess.core.repository.ControllePtsRepository;
import com.teknokote.ess.core.repository.FuelGradesRepository;
import com.teknokote.ess.core.repository.NozzleRepository;
import com.teknokote.ess.core.repository.TankRepository;
import com.teknokote.ess.dto.ControllerPtsDto;
import com.teknokote.ess.dto.NozzelConfigDto;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSPumpNozzles;
import com.teknokote.pts.client.response.configuration.PTSPumpNozzlesResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSPumpNozzlesResponsePacketData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NozzleServiceTest {

    @Mock
    private NozzleRepository nozzleRepository;
    @Mock
    private PumpService pumpService;
    @Mock
    private FuelGradesRepository gradesRepository;
    @Mock
    private TankRepository tankRepository;
    @Mock
    private ControllerDao controllerDao;
    @Mock
    private ControllePtsRepository controllePtsRepository;
    @Mock
    private ControllerPtsConfigurationService controllerPtsConfigurationService;
    @InjectMocks
    private NozzleService nozzleService;

    @Test
    void testFindAllFuelIdsByConfigurationAndPumpId() {
        // Given
        Long pumpIdConf = 1L;
        String configurationId = "config123";
        String ptsId = "pts123";
        List<Long> expectedIds = Arrays.asList(1L, 2L, 3L);
        when(nozzleRepository.findAllFuelIdsByConfigurationAndPumpId(pumpIdConf, configurationId, ptsId)).thenReturn(expectedIds);

        // When
        List<Long> result = nozzleService.findAllFuelIdsByConfigurationAndPumpId(pumpIdConf, configurationId, ptsId);

        // Then
        assertNotNull(result);
        assertEquals(expectedIds.size(), result.size());
        assertIterableEquals(expectedIds, result);
    }

    @Test
    void testFindAllFuelByConfigurationAndPumpId() {
        // Given
        Long pumpIdConf = 1L;
        String configurationId = "config123";
        String ptsId = "pts123";
        List<String> expectedFuels = Arrays.asList("Petrol", "Diesel");
        when(nozzleRepository.findAllFuelByConfigurationAndPumpId(pumpIdConf, configurationId, ptsId)).thenReturn(expectedFuels);

        // When
        List<String> result = nozzleService.findAllFuelByConfigurationAndPumpId(pumpIdConf, configurationId, ptsId);

        // Then
        assertNotNull(result);
        assertEquals(expectedFuels.size(), result.size());
        assertIterableEquals(expectedFuels, result);
    }

    @Test
    void testFindNozzlesByControllerOnCurrentConfiguration() {
        // Given
        Long idCtr = 1L;
        ControllerPtsConfiguration currentConfiguration = new ControllerPtsConfiguration();
        when(controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr)).thenReturn(currentConfiguration);
        Nozzle nozzle = new Nozzle();
        List<Nozzle> expectedNozzles = Collections.singletonList(nozzle);
        when(nozzleRepository.findNozzlesByControllerConfiguration(currentConfiguration)).thenReturn(expectedNozzles);

        // When
        List<Nozzle> result = nozzleService.findNozzlesByControllerOnCurrentConfiguration(idCtr);

        // Then
        assertNotNull(result);
        assertEquals(expectedNozzles.size(), result.size());
        assertEquals(expectedNozzles.get(0), result.get(0));
    }

    @Test
    void testGetAll() {
        // Given
        Nozzle nozzle1 = new Nozzle();
        Nozzle nozzle2 = new Nozzle();
        List<Nozzle> expectedNozzles = Arrays.asList(nozzle1, nozzle2);
        when(nozzleRepository.findAll()).thenReturn(expectedNozzles);

        // When
        List<Nozzle> result = nozzleService.getAll();

        // Then
        assertNotNull(result);
        assertEquals(expectedNozzles.size(), result.size());
        assertIterableEquals(expectedNozzles, result);
    }

    @Test
    void testFindNozzleByIdConfAndPump() {
        // Given
        Long idConf = 1L;
        Long pumpIdConf = 2L;
        String configurationId = "config123";
        String ptsId = "pts123";
        Nozzle expectedNozzle = new Nozzle();
        when(nozzleRepository.findByIdConfAndPumpAndConfigurationIdAndControllerPts(idConf, pumpIdConf, configurationId, ptsId)).thenReturn(expectedNozzle);

        // When
        Nozzle result = nozzleService.findNozzleByIdConfAndPump(idConf, pumpIdConf, configurationId, ptsId);

        // Then
        assertNotNull(result);
        assertEquals(expectedNozzle, result);
    }

    @Test
    void testFindNozzleByPump() {
        // Given
        Long idCtr = 1L;
        Long pumpIdConf = 2L;
        ControllerPtsDto controllerPtsDto = ControllerPtsDto.builder().build();
        controllerPtsDto.setCurrentConfigurationId(1L);
        when(controllerDao.findById(idCtr)).thenReturn(Optional.of(controllerPtsDto));
        Nozzle nozzle = new Nozzle();
        List<Nozzle> expectedNozzles = Collections.singletonList(nozzle);
        when(nozzleRepository.findNozzleByPump(idCtr, controllerPtsDto.getCurrentConfigurationId(), pumpIdConf)).thenReturn(expectedNozzles);

        // When
        List<Nozzle> result = nozzleService.findNozzleByPump(idCtr, pumpIdConf);

        // Then
        assertNotNull(result);
        assertEquals(expectedNozzles.size(), result.size());
        assertEquals(expectedNozzles.get(0), result.get(0));
    }

    @Test
    void testFindFuelByNozzleAndPump() {
        // Given
        Long nozzleIdConf = 1L;
        Long pumpIdConf = 2L;
        String configurationId = "config123";
        String ptsId = "pts123";
        FuelGrade expectedFuelGrade = new FuelGrade();
        when(nozzleRepository.findFuelByNozzleAndPump(nozzleIdConf, pumpIdConf, configurationId, ptsId)).thenReturn(expectedFuelGrade);

        // When
        FuelGrade result = nozzleService.findFuelByNozzleAndPump(nozzleIdConf, pumpIdConf, configurationId, ptsId);

        // Then
        assertNotNull(result);
        assertEquals(expectedFuelGrade, result);
    }

    @Test
    void testAddNewPumpNozzles() {
        // Given
        ControllerPtsConfiguration controllerPtsConfiguration = new ControllerPtsConfiguration();
        JsonPTSResponse jsonResponse = new JsonPTSResponse();
        jsonResponse.setPackets(new ArrayList<>());

        PTSPumpNozzlesResponsePacket responsePacket = mock(PTSPumpNozzlesResponsePacket.class);
        PTSPumpNozzlesResponsePacketData data = mock(PTSPumpNozzlesResponsePacketData.class);
        PTSPumpNozzles pumpNozzles = mock(PTSPumpNozzles.class);

        // Mocking the response structure
        jsonResponse.getPackets().add(responsePacket);
        when(responsePacket.getData()).thenReturn(data);
        when(data.getPumpNozzles()).thenReturn(Collections.singletonList(pumpNozzles));

        // Mocking behavior of the pumpNozzle
        when(pumpNozzles.getFuelGradeIds()).thenReturn(Arrays.asList(1L, 2L));
        when(pumpNozzles.getTankIds()).thenReturn(Arrays.asList(3L, 0L)); // one tank id is zero (not configured)

        // Mocking behavior for the Tank
        Tank tank = new Tank();
        when(tankRepository.findAllByIdConfAndControllerPtsConfiguration(3L, controllerPtsConfiguration)).thenReturn(tank);

        // Pump ID
        Long pumpId = 10L;
        when(pumpNozzles.getPumpId()).thenReturn(pumpId);
        when(pumpService.findAllByIdConfAndControllerPtsConfiguration(pumpId, controllerPtsConfiguration)).thenReturn(new Pump());

        // When
        JsonPTSResponse resultResponse = nozzleService.addNewPumpNozzles(controllerPtsConfiguration, jsonResponse);

        // Then
        assertNotNull(resultResponse);
        verify(nozzleRepository, times(2)).save(any(Nozzle.class));    }

    @Test
    void testAddNozzle() {
        // Given
        ControllerPtsConfiguration controllerPtsConfiguration = new ControllerPtsConfiguration();
        PTSPumpNozzles pumpNozzle = mock(PTSPumpNozzles.class);
        Long pumpIdConf = 1L;

        // Mock behavior
        when(pumpNozzle.getFuelGradeIds()).thenReturn(Arrays.asList(1L, 2L));
        when(pumpNozzle.getPumpId()).thenReturn(pumpIdConf);
        when(pumpNozzle.getTankIds()).thenReturn(Arrays.asList(3L, 0L)); // one tank id is zero, not for configuration

        // Mocking behavior for the Pump and Tank
        Pump pump = new Pump();
        when(pumpService.findAllByIdConfAndControllerPtsConfiguration(pumpIdConf, controllerPtsConfiguration)).thenReturn(pump);
        Tank tank = mock(Tank.class);
        when(tankRepository.findAllByIdConfAndControllerPtsConfiguration(3L, controllerPtsConfiguration)).thenReturn(tank);

        // When
        List<Nozzle> createdNozzles = nozzleService.addNozzle(pumpNozzle, controllerPtsConfiguration);

        // Then
        assertNotNull(createdNozzles);
        assertEquals(2, createdNozzles.size()); // Expecting 2 nozzles to be created

        // Verify that Nozzle creation method was called
        verify(nozzleRepository, times(2)).save(any(Nozzle.class));
    }
    @Test
    void testMapToNozzelconfigDto_ValidNozzle() {
        // Given
        FuelGrade fuelGrade = new FuelGrade();
        fuelGrade.setName("Diesel");
        fuelGrade.setIdConf(1L);

        Pump pump = new Pump();
        pump.setIdConf(2L);

        Nozzle nozzle = new Nozzle();
        nozzle.setGrade(fuelGrade);
        nozzle.setPump(pump);
        nozzle.setId(3L);
        nozzle.setIdConf(4L);

        // When
        NozzelConfigDto result = nozzleService.mapToNozzelconfigDto(nozzle);

        // Then
        assertNotNull(result);
        assertEquals("Diesel", result.getFuelGrad());
        assertEquals("1", result.getFuelId());
        assertEquals("2", result.getPump());
        assertEquals(3L, result.getId());
        assertEquals(4L, result.getIdConf());
    }

    @Test
    void testMapToNozzelconfigDto_NoTank() {
        // Given
        FuelGrade fuelGrade = new FuelGrade();
        fuelGrade.setName("Petrol");
        fuelGrade.setIdConf(5L);

        Pump pump = new Pump();
        pump.setIdConf(6L);

        Nozzle nozzle = new Nozzle();
        nozzle.setGrade(fuelGrade);
        nozzle.setPump(pump);
        nozzle.setId(7L);
        nozzle.setIdConf(8L);
        nozzle.setTank(null); // Explicitly setting to null

        // When
        NozzelConfigDto result = nozzleService.mapToNozzelconfigDto(nozzle);

        // Then
        assertNotNull(result);
        assertEquals("Petrol", result.getFuelGrad());
        assertEquals("5", result.getFuelId());
        assertEquals("6", result.getPump());
        assertEquals(7L, result.getId());
        assertEquals(8L, result.getIdConf());
        assertNull(result.getTank()); // expects null or not set
    }

    @Test
    void testMapToNozzelconfigDto_NoFuelGrade() {
        // Given
        Nozzle nozzle = new Nozzle();
        nozzle.setGrade(null); // Setting FuelGrade to null

        Pump pump = new Pump();
        pump.setIdConf(9L);
        nozzle.setPump(pump);
        nozzle.setId(10L);
        nozzle.setIdConf(11L);

        // When
        NozzelConfigDto result = nozzleService.mapToNozzelconfigDto(nozzle);

        // Then
        assertNotNull(result);
        assertEquals("", result.getFuelGrad()); // FuelGrad should be empty due to exception
        assertNull(result.getFuelId()); // As FuelGrade is null
        assertEquals("9", result.getPump());
        assertEquals(10L, result.getId());
        assertEquals(11L, result.getIdConf());
    }

    @Test
    void testFindAllFuelGradesByPump() {
        // Given
        Long pumpIdConf = 1L;
        String configurationId = "config123";
        String ptsId = "pts123";
        List<FuelGrade> expectedGrades = Arrays.asList(new FuelGrade(), new FuelGrade());

        // Mocking the behavior of the repository
        when(nozzleRepository.findAllFuelGradesByPump(pumpIdConf, configurationId, ptsId)).thenReturn(expectedGrades);

        // When
        List<FuelGrade> result = nozzleService.findAllFuelGradesByPump(pumpIdConf, configurationId, ptsId);

        // Then
        assertNotNull(result);
        assertEquals(expectedGrades.size(), result.size());
        assertIterableEquals(expectedGrades, result);
    }

    @Test
    void testFindAllFuelGradesByPump_EmptyList() {
        // Given
        Long pumpIdConf = 1L;
        String configurationId = "config123";
        String ptsId = "pts123";
        List<FuelGrade> expectedGrades = Collections.emptyList();

        // Mocking the behavior of the repository
        when(nozzleRepository.findAllFuelGradesByPump(pumpIdConf, configurationId, ptsId)).thenReturn(expectedGrades);

        // When
        List<FuelGrade> result = nozzleService.findAllFuelGradesByPump(pumpIdConf, configurationId, ptsId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}   