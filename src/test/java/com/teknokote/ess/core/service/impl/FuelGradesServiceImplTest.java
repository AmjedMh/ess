package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.repository.FuelGradesRepository;
import com.teknokote.ess.core.repository.transactions.TransactionRepository;
import com.teknokote.ess.dto.FuelGradeConfigDto;
import com.teknokote.ess.dto.charts.SalesGradesDto;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSFuelGrade;
import com.teknokote.pts.client.response.configuration.PTSFuelGradesConfigurationResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSFuelGradesConfigurationResponsePacketData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuelGradesServiceImplTest {
    @InjectMocks
    private FuelGradesServiceImpl fuelGradesService;
    @Mock
    private FuelGradesRepository fuelGradesRepository;
    @Mock
    private ControllerPtsConfigurationService controllerPtsConfigurationService;
    @Mock
    private PumpService pumpService;
    @Mock
    private NozzleService nozzleService;
    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void addNewFuelGrades_ShouldSaveNewFuelGrades() {
        ControllerPtsConfiguration configuration = mock(ControllerPtsConfiguration.class);
        JsonPTSResponse response = mock(JsonPTSResponse.class);
        PTSFuelGrade mockFuelGrade = mock(PTSFuelGrade.class);

        when(mockFuelGrade.getId()).thenReturn(1L);
        when(mockFuelGrade.getName()).thenReturn("Gasoil");
        when(mockFuelGrade.getPrice()).thenReturn(2.5);

        PTSFuelGradesConfigurationResponsePacketData data = mock(PTSFuelGradesConfigurationResponsePacketData.class);
        when(data.getFuelGrades()).thenReturn(Collections.singletonList(mockFuelGrade));

        PTSFuelGradesConfigurationResponsePacket packet = mock(PTSFuelGradesConfigurationResponsePacket.class);
        when(packet.getData()).thenReturn(data);

        when(response.getPackets()).thenReturn(Collections.singletonList(packet));

        fuelGradesService.addNewFuelGrades(configuration, response);

        verify(fuelGradesRepository, times(1)).save(any(FuelGrade.class));
    }
    @Test
    void findFuelGradeByIdConfAndController_ShouldReturnFuelGrade() {
        Long idConf = 1L;
        String configurationId = "150e9c08";
        Long idCtr = 1L;
        FuelGrade fuelGrade = new FuelGrade();
        fuelGrade.setIdConf(idConf);

        when(fuelGradesRepository.findFuelGradeByIdConfAndController(idConf, configurationId, idCtr)).thenReturn(fuelGrade);

        FuelGrade result = fuelGradesService.findFuelGradeByIdConfAndController(idConf, configurationId, idCtr);

        assertNotNull(result);
        assertEquals(idConf, result.getIdConf());
    }
    @Test
    void mapToFuelGradeConfigDto_ShouldReturnCorrectDto() {
        FuelGrade fuelGrade = new FuelGrade();
        fuelGrade.setIdConf(1L);
        fuelGrade.setName("Gasoil");
        fuelGrade.setPrice(2.5);
        fuelGrade.setExpansionCoefficient(0.9);

        FuelGradeConfigDto dto = fuelGradesService.mapToFuelGradeConfigDto(fuelGrade);

        assertNotNull(dto);
        assertEquals(fuelGrade.getIdConf(), dto.getIdConf());
        assertEquals(fuelGrade.getName(), dto.getName());
        assertEquals(fuelGrade.getPrice(), dto.getPrice());
        assertEquals(fuelGrade.getExpansionCoefficient(), dto.getExpansionCoefficient());
    }
    @Test
    void getSalesByGrades_ShouldThrowExceptionWhenDatesAreNull() {
        Long idCtr = 1L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                fuelGradesService.getSalesByGrades(idCtr, null, null));

        assertEquals("startDate and endDate must not be null", exception.getMessage());
    }
    @Test
    void addFuelGrade_ShouldSaveExistingOrNewGrades() {
        // Mock the input objects
        PTSFuelGrade mockFuelGrade = mock(PTSFuelGrade.class);
        ControllerPtsConfiguration configuration = mock(ControllerPtsConfiguration.class);
        ControllerPts mockControllerPts = mock(ControllerPts.class);

        // Stubbing the mockFuelGrade
        when(mockFuelGrade.getId()).thenReturn(1L);
        when(mockFuelGrade.getName()).thenReturn("Gasoil");
        when(mockFuelGrade.getPrice()).thenReturn(2.5);
        when(mockFuelGrade.getExpansionCoefficient()).thenReturn(0.9);

        // Mocking the configuration to return the mockControllerPts
        when(configuration.getControllerPts()).thenReturn(mockControllerPts);
        lenient().when(mockControllerPts.getId()).thenReturn(1L);

        // Ensure that findAllByIdConfAndControllerPtsConfiguration returns null for not found
        when(fuelGradesRepository.findAllByIdConfAndControllerPtsConfiguration(1L, configuration)).thenReturn(null); // Ensure this is needed

        // Creating a hypothetical response for save
        FuelGrade newFuelGrade = new FuelGrade();
        newFuelGrade.setName("Gasoil");
        newFuelGrade.setPrice(2.5);
        newFuelGrade.setExpansionCoefficient(0.9);

        // Mocking the repository's save method
        when(fuelGradesRepository.save(any(FuelGrade.class))).thenReturn(newFuelGrade); // Ensure this is actually called in the service method

        // Invoking the method being tested
        FuelGrade result = fuelGradesService.addFuelGrade(mockFuelGrade, configuration);

        // Asserting expected results
        assertNotNull(result);
        assertEquals("Gasoil", result.getName());

        // Verify save is called once
        verify(fuelGradesRepository, times(1)).save(any(FuelGrade.class));
    }
    @Test
    void findFuelGradeByConfId_ShouldReturnFuelGrade() {
        Long idConf = 1L;
        FuelGrade fuelGrade = new FuelGrade();
        fuelGrade.setIdConf(idConf);

        when(fuelGradesRepository.findFuelGradeByConfId(idConf)).thenReturn(fuelGrade);

        FuelGrade result = fuelGradesService.findFuelGradeByConfId(idConf);

        assertNotNull(result);
        assertEquals(idConf, result.getIdConf());
    }

    @Test
    void findFuelGradesByControllerOnCurrentConfiguration_ShouldReturnListOfFuelGrades() {
        Long idCtr = 1L;

        // Mock the current configuration returned by the service
        ControllerPtsConfiguration configuration = new ControllerPtsConfiguration();
        configuration.setConfigurationId("cfgId");
        configuration.setPtsId("pts1");

        // Setup mock return for the configuration service
        when(controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr)).thenReturn(configuration);

        // Set up a mock return for the repository method
        when(fuelGradesRepository.findFuelGradesByControllerConfiguration(configuration)).thenReturn(Collections.singletonList(new FuelGrade()));

        // Invoke the service method
        List<FuelGrade> result = fuelGradesService.findFuelGradesByControllerOnCurrentConfiguration(idCtr);

        // Assert the results and verify the interactions
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(fuelGradesRepository, times(1)).findFuelGradesByControllerConfiguration(configuration);
    }

    @Test
    void findAllByIdConfAndControllerPtsConfiguration_ShouldReturnCorrectFuelGrade() {
        Long fuelGradeId = 1L;
        ControllerPtsConfiguration configuration = mock(ControllerPtsConfiguration.class);
        FuelGrade expectedFuelGrade = new FuelGrade();
        expectedFuelGrade.setIdConf(fuelGradeId);

        when(fuelGradesRepository.findAllByIdConfAndControllerPtsConfiguration(fuelGradeId, configuration)).thenReturn(expectedFuelGrade);

        FuelGrade result = fuelGradesService.findAllByIdConfAndControllerPtsConfiguration(fuelGradeId, configuration);

        assertNotNull(result);
        assertEquals(fuelGradeId, result.getIdConf());
        verify(fuelGradesRepository, times(1)).findAllByIdConfAndControllerPtsConfiguration(fuelGradeId, configuration);
    }
    @Test
    void testProcessChartSalesByFuelGrade() {
        // Mock data for repository method
        when(transactionRepository.getVolumeSalesforPeriod(eq(1L), any(), any(), eq(1L), eq("FuelA")))
                .thenReturn(Collections.singletonList(new Object[]{1L, "FuelA", LocalDateTime.now(), 100.0, 250.0}));

        when(transactionRepository.getVolumeSalesforPeriod(eq(1L), any(), any(), eq(1L), eq("FuelB")))
                .thenReturn(Collections.singletonList(new Object[]{2L, "FuelB", LocalDateTime.now(), 50.0, 125.0}));

        // Define the input parameters
        Long idCtr = 1L;
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 1, 31, 23, 59);

        // Create pumpToFuelIdsMap as expected by the method
        Map<Long, List<String>> pumpToFuelIdsMap = new HashMap<>();
        pumpToFuelIdsMap.put(1L, Arrays.asList("FuelA", "FuelB"));

        // Call the method
        List<SalesGradesDto> result = fuelGradesService.processChartSalesByFuelGrade(idCtr, startDate, endDate, pumpToFuelIdsMap);

        // Assertions
        assertNotNull(result);
        assertEquals(2, result.size());

        SalesGradesDto fuelA = result.stream().filter(dto -> dto.getFuelGrade().equals("FuelA")).findFirst().orElse(null);
        assertNotNull(fuelA);
        assertEquals(250.0, fuelA.getTotalSalesParAmount(), 0.001);
        assertEquals(100.0, fuelA.getTotalSalesParVolume(), 0.001);

        SalesGradesDto fuelB = result.stream().filter(dto -> dto.getFuelGrade().equals("FuelB")).findFirst().orElse(null);
        assertNotNull(fuelB);
        assertEquals(125.0, fuelB.getTotalSalesParAmount(), 0.001);
        assertEquals(50.0, fuelB.getTotalSalesParVolume(), 0.001);
    }

    @Test
    void getSalesByGrades_ShouldReturnEmptyList_WhenConfigurationNotFound() {
        Long idCtr = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        // Mock the behavior of configuration service to return null
        when(controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr)).thenReturn(null);

        // Invoke the service method
        List<SalesGradesDto> result = fuelGradesService.getSalesByGrades(idCtr, startDate, endDate);

        // Verify that an empty list is returned
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
