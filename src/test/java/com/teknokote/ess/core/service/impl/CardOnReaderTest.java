package com.teknokote.ess.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.core.dao.StationDao;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.configuration.Nozzle;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.repository.NozzleRepository;
import com.teknokote.ess.core.repository.PumpRepository;
import com.teknokote.ess.core.service.PumpAttendantService;
import com.teknokote.ess.dto.ControllerPtsDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.events.listeners.CardOnReaderEventListener;
import com.teknokote.ess.events.listeners.PumpAuthorizationInfo;
import com.teknokote.ess.events.listeners.WebSocketMessageEvent;
import com.teknokote.ess.events.publish.CustomerAccountAuthorization;
import com.teknokote.ess.events.publish.cm.*;
import com.teknokote.ess.events.types.CardOnReaderEvent;
import com.teknokote.ess.wsserveur.ESSWebSocketHandler;
import com.teknokote.pts.client.upload.status.FillingStatus;
import com.teknokote.pts.client.upload.status.Pumps;
import com.teknokote.pts.client.upload.status.UploadStatusRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.teknokote.ess.integration.TestUtils.readFile;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
 class CardOnReaderTest {

    @InjectMocks
    private CardOnReaderEventListener cardOnReaderEventListener;
    @Mock
    private ESSWebSocketHandler essWebSocketHandler;
    @Mock
    private UploadStatusRequest query;
    @Mock
    private StationDao stationDao;
    @Mock
    private PumpRepository pumpRepository;
    @Mock
    private NozzleRepository nozzleRepository;
    @Mock
    private CustomerAccountAuthorization customerAccountAuthorization;
    @Mock
    private PumpAttendantService pumpAttendantService;
    @Mock
    private ControllerPtsConfiguration controllerPtsConfiguration;
    @Mock
    private StationDto stationDto;
    @Mock
    private Pump pump;
    private static String pumpTransactionMessage;
    private static String ptsId;
    private static String configurationId;
    private Map<String, Map<Long, PumpAuthorizationInfo>> pumpAuthorizationMap;

    @Mock
    private CardOnReaderEvent event;
    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(cardOnReaderEventListener, "pumpAuthorizationMap", pumpAuthorizationMap);
        // Define the JSON string directly here
        pumpTransactionMessage = """
        {
          "Protocol": "jsonPTS",
          "PtsId": "0027003A3438510935383135",
          "Packets": [
            {
              "Id": 12154,
              "Type": "UploadPumpTransaction",
              "Data": {
                "DateTime": "2023-09-07T08:48:40",
                "Pump": 1,
                "Nozzle": 1,
                "FuelGradeId": 1,
                "FuelGradeName": "Gasoil",
                "Transaction": 10376,
                "UserId": 1,
                "Tank": 1,
                "Volume": 11.0,
                "Amount": 23100.0,
                "Price": 2100.0,
                "TotalVolume": 11.0,
                "TotalAmount": 23100.0,
                "DateTimeStart": "2023-09-07T08:48:27",
                "TCVolume": 0.0,
                "Tag": "",
                "ConfigurationId": "699c8c2"
              }
            }
          ]
        }
        """;
        ptsId = "0027003A3438510935383135";
        configurationId= "699c8c2";
        // Mock ControllerPtsConfiguration with values
        when(controllerPtsConfiguration.getPtsId()).thenReturn(ptsId);
        when(controllerPtsConfiguration.getConfigurationId()).thenReturn(configurationId);
        query = new ObjectMapper().readValue(readFile("/uploads/upload-status.json"), UploadStatusRequest.class);
        ControllerPtsDto controllerPtsDto = Mockito.mock(ControllerPtsDto.class);
        when(controllerPtsDto.getId()).thenReturn(1L);
        when(controllerPtsDto.getPtsId()).thenReturn(ptsId);
        when(stationDto.getId()).thenReturn(1L);
        when(stationDto.getCustomerAccountId()).thenReturn(1L);
        when(stationDto.getName()).thenReturn("stationTest");
        when(stationDto.getControllerPts()).thenReturn(controllerPtsDto);
        when(stationDto.getIdentifier()).thenReturn("STNA");
        when(pump.getControllerPtsConfiguration()).thenReturn(controllerPtsConfiguration);
        when(pump.getIdConf()).thenReturn(1L);

        // Mock Nozzle and related entities
        Nozzle nozzle = Mockito.mock(Nozzle.class);
        when(nozzle.getIdConf()).thenReturn(1L);
        FuelGrade fuelGrade = new FuelGrade();
        fuelGrade.setIdConf(1L);
        fuelGrade.setName("Gasoil");
        nozzle.setGrade(fuelGrade);
        when(pumpRepository.findByIdConfAndControllerPtsConfiguration_ConfigurationId(pump.getIdConf(), controllerPtsConfiguration.getConfigurationId())).thenReturn(pump);
        when(nozzleRepository.findAllByPump_IdConfAndAndControllerPtsConfiguration_ConfigurationId(pump.getIdConf(),controllerPtsConfiguration.getConfigurationId())).thenReturn(List.of(nozzle));
        when(stationDao.findByControllerPtsId(controllerPtsConfiguration.getPtsId())).thenReturn(stationDto);
    }

    // Test for handleCardOnReaderEvent
    @Test
    void testHandleCardOnReaderEvent() {
        controllerPtsConfiguration = Mockito.mock(ControllerPtsConfiguration.class);
        when(event.getUploadStatusRequest()).thenReturn(query);
        when(event.getControllerPtsConfiguration()).thenReturn(controllerPtsConfiguration);
        cardOnReaderEventListener.handleCardOnReaderEvent(event);
        assertEquals(4,query.getPackets().get(0).getData().getPumps().getIdleStatus().getIds().size());
    }

    // Test for authoriseCardOnIdleStatus
    @Test
    void testAuthoriseCardOnIdleStatus() {
        Pumps pumps = query.getPackets().get(0).getData().getPumps();
        cardOnReaderEventListener.authoriseCardOnIdleStatus(pumps, query, stationDto, controllerPtsConfiguration);
        assertNotNull(pumps);
    }

    // Test for processPumpAuthorization
    @Test
    void testProcessPumpAuthorization() {
        String tag = "tag1";
        controllerPtsConfiguration = Mockito.mock(ControllerPtsConfiguration.class);

        when(pump.getIdConf()).thenReturn(1L);

        cardOnReaderEventListener.processPumpAuthorization(tag, pump.getIdConf(), query, stationDto, controllerPtsConfiguration);

        assertNotNull(pump);
    }


    // Test for authorizeIfNoPumpAttendant
    @Test
    void testAuthorizeIfNoPumpAttendant() {
        Long cardId = 2L;
        Long authorizationId = 1L;
        Long transactionId = 10376L;
        pump = Mockito.mock(Pump.class);
        Nozzle nozzle = Mockito.mock(Nozzle.class);
        nozzle.setIdConf(1L);
        FuelGrade fuelGrade = Mockito.mock(FuelGrade.class);
        controllerPtsConfiguration = Mockito.mock(ControllerPtsConfiguration.class);
        String tag = "tag1";
        when(pumpAttendantService.findPumpAttandantByStationAndTag(stationDto.getId(), tag)).thenReturn(Optional.empty());
        AuthorizationDto authorizationDto = AuthorizationDto.builder()
                .status(EnumAuthorizationStatus.GRANTED)
                .quantity(BigDecimal.valueOf(2.0))
                .cardType(EnumCardType.QUANTITY)
                .id(1L)
                .cardId(1L)
                .ptsId(ptsId)
                .pump(pump.getIdConf()).build();
        Map<Long, PumpAuthorizationInfo> pumpMap = new HashMap<>();
        PumpAuthorizationInfo authorizationInfo = new PumpAuthorizationInfo(authorizationId,cardId,1L,transactionId,EnumCardStatus.AUTHORIZED);
        pumpMap.put(pump.getIdConf(), authorizationInfo);

        pumpAuthorizationMap = new HashMap<>();
        pumpAuthorizationMap.put(ptsId, pumpMap);
        ReflectionTestUtils.setField(cardOnReaderEventListener, "pumpAuthorizationMap", pumpAuthorizationMap);
        when(customerAccountAuthorization.requestAuthorization(any(AuthorizationRequest.class)))
                .thenReturn(authorizationDto);
        cardOnReaderEventListener.authorizeIfNoPumpAttendant(pump, nozzle, fuelGrade, tag, stationDto, controllerPtsConfiguration);

        verify(customerAccountAuthorization, times(1)).requestAuthorization(any(AuthorizationRequest.class));
    }

    // Test for blockCardOnFillingStatus
    @Test
    void testBlockCardOnFillingStatus() {
        Pumps pumps = Mockito.mock(Pumps.class);
        controllerPtsConfiguration = Mockito.mock(ControllerPtsConfiguration.class);
        when(controllerPtsConfiguration.getPtsId()).thenReturn(ptsId);
        when(controllerPtsConfiguration.getConfigurationId()).thenReturn(configurationId);
        controllerPtsConfiguration.setPtsId(ptsId);
        FillingStatus fillingStatus = Mockito.mock(FillingStatus.class);
        List<Long> pumpIds = Arrays.asList(1L);
        List<Long> transactions = Arrays.asList(10376L);
        Long cardId = 2L;
        Long authorizationId = 1L;
        Long transactionId = 10376L;
        when(pumps.getFillingStatus()).thenReturn(fillingStatus);
        when(fillingStatus.getIds()).thenReturn(pumpIds);
        when(fillingStatus.getTransactions()).thenReturn(transactions);
        Map<Long, PumpAuthorizationInfo> pumpMap = new HashMap<>();
        PumpAuthorizationInfo authorizationInfo = new PumpAuthorizationInfo(authorizationId,cardId,1L,transactionId,EnumCardStatus.CONFIRMATION);
        pumpMap.put(pump.getIdConf(), authorizationInfo);

        pumpAuthorizationMap = new HashMap<>();
        pumpAuthorizationMap.put(ptsId, pumpMap);
        ReflectionTestUtils.setField(cardOnReaderEventListener, "pumpAuthorizationMap", pumpAuthorizationMap);

        Mockito.doNothing().when(customerAccountAuthorization).updateCardStatus(2L, authorizationId,transactionId ,EnumCardStatus.IN_USE);
        cardOnReaderEventListener.blockCardOnFillingStatus(pumps, controllerPtsConfiguration);

        // Verify the update method is called for the card status
        verify(customerAccountAuthorization, times(1)).updateCardStatus(2L, authorizationId,transactionId ,EnumCardStatus.IN_USE );
    }

    // Test for handleMessageEvent
    @Test
    void testHandleMessageEvent() throws IOException {
        WebSocketMessageEvent webSocketMessageEvent = Mockito.mock(WebSocketMessageEvent.class);
        Long cardId = 2L;
        Long authorizationId = 1L;
        Long transactionId = 10376L;

        // Set up the event to return a specific message
        when(webSocketMessageEvent.getMessage()).thenReturn(pumpTransactionMessage);

        // Create the pump authorization map and authorization info
        Map<Long, PumpAuthorizationInfo> pumpMap = new HashMap<>();
        PumpAuthorizationInfo authorizationInfo = new PumpAuthorizationInfo(authorizationId, cardId, 1L, transactionId, EnumCardStatus.AUTHORIZED);
        pumpMap.put(pump.getIdConf(), authorizationInfo);

        // Initialize the pump authorization map
        pumpAuthorizationMap = new HashMap<>();
        pumpAuthorizationMap.put(ptsId, pumpMap);

        ReflectionTestUtils.setField(cardOnReaderEventListener, "pumpAuthorizationMap", pumpAuthorizationMap);

        cardOnReaderEventListener.handleMessageEvent(webSocketMessageEvent);

        assertNotNull(pumpAuthorizationMap);
        assertTrue(pumpAuthorizationMap.containsKey(ptsId));

        // Assert that the pumpMap contains the correct pump authorization info
        Map<Long, PumpAuthorizationInfo> updatedPumpMap = pumpAuthorizationMap.get(ptsId);
        assertNotNull(updatedPumpMap);
        assertTrue(updatedPumpMap.containsKey(pump.getIdConf()));

        verify(webSocketMessageEvent, times(1)).getMessage();
    }


    // Test for handlePumpAuthorizeConfirmation
    @Test
    void testHandlePumpAuthorizeConfirmation() {
        Long cardId = 2L;
        Long authorizationId = 1L;
        Long pumpId = 1L;
        Long responseId = 1234L;
        Long transactionId = 10376L;

        Map<Long, PumpAuthorizationInfo> pumpMap = new HashMap<>();
        PumpAuthorizationInfo authorizationInfo = new PumpAuthorizationInfo(authorizationId,cardId,1234L,transactionId,EnumCardStatus.AUTHORIZED);
        pumpMap.put(pump.getIdConf(), authorizationInfo);

        pumpAuthorizationMap = new HashMap<>();
        pumpAuthorizationMap.put(ptsId, pumpMap);
        ReflectionTestUtils.setField(cardOnReaderEventListener, "pumpAuthorizationMap", pumpAuthorizationMap);


        cardOnReaderEventListener.handlePumpAuthorizeConfirmation(ptsId, pumpId, responseId, transactionId);

        // Verify that the status is updated to CONFIRMATION
        verify(customerAccountAuthorization, times(1)).updateCardStatus(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.eq(EnumCardStatus.CONFIRMATION));
    }
}
