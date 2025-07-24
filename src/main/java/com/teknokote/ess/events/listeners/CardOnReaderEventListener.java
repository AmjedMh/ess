package com.teknokote.ess.events.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.core.dao.StationDao;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.configuration.Nozzle;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.repository.NozzleRepository;
import com.teknokote.ess.core.repository.PumpRepository;
import com.teknokote.ess.core.service.PumpAttendantService;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.events.publish.CustomerAccountAuthorization;
import com.teknokote.ess.events.publish.cm.*;
import com.teknokote.ess.events.types.CardOnReaderEvent;
import com.teknokote.ess.wsserveur.ESSWebSocketHandler;
import com.teknokote.pts.client.request.authorize.PumpAuthorizeRequestPacket;
import com.teknokote.pts.client.upload.status.FillingStatus;
import com.teknokote.pts.client.upload.status.Pumps;
import com.teknokote.pts.client.upload.status.UploadStatusRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class CardOnReaderEventListener {
    @Autowired
    private ESSWebSocketHandler essWebSocketHandler;
    @Autowired
    private StationDao stationDao;
    @Autowired
    private PumpRepository pumpRepository;
    @Autowired
    private NozzleRepository nozzleRepository;
    @Autowired
    private CustomerAccountAuthorization customerAccountAuthorization;
    @Autowired
    private PumpAttendantService pumpAttendantService;
    private final Map<String, Map<Long, PumpAuthorizationInfo>> pumpAuthorizationMap = new ConcurrentHashMap<>();

    private static final Long FIXED_REQUEST_ID = 1234L;


    @EventListener
    public void handleCardOnReaderEvent(CardOnReaderEvent cardOnReaderEvent) {
        final UploadStatusRequest query = cardOnReaderEvent.getUploadStatusRequest();
        final ControllerPtsConfiguration controllerPtsConfiguration = cardOnReaderEvent.getControllerPtsConfiguration();
        StationDto stationDto = stationDao.findByControllerPtsId(controllerPtsConfiguration.getPtsId());
        Pumps pumps = query.getPackets().get(0).getData().getPumps();
        authoriseCardOnIdleStatus(pumps,query,stationDto,controllerPtsConfiguration);
        blockCardOnFillingStatus(pumps,controllerPtsConfiguration);
        }

    public void authoriseCardOnIdleStatus(Pumps pumps, UploadStatusRequest query, StationDto stationDto, ControllerPtsConfiguration controllerPtsConfiguration) {
        Optional.ofNullable(pumps.getIdleStatus().getTags())
                .filter(tags -> !tags.isEmpty())
                .ifPresent(tags -> processPumpAuthorization(tags.get(0), pumps.getIdleStatus().getIds().get(0), query, stationDto, controllerPtsConfiguration));
    }

    public void processPumpAuthorization(String tag, Long pumpId, UploadStatusRequest query, StationDto stationDto, ControllerPtsConfiguration controllerPtsConfiguration) {
        String configurationId = query.getPackets().get(0).getData().getConfigurationId();
        Pump pump = pumpRepository.findByIdConfAndControllerPtsConfiguration_ConfigurationId(pumpId, configurationId);

        if (pump == null) return;

        List<Nozzle> nozzleList = nozzleRepository.findAllByPump_IdConfAndAndControllerPtsConfiguration_ConfigurationId(pump.getIdConf(), configurationId);
        Optional<Nozzle> nozzleOptional = nozzleList.stream().findFirst();
        Optional<FuelGrade> fuelGradeOptional = nozzleList.stream().map(Nozzle::getGrade).filter(Objects::nonNull).findFirst();

        if (nozzleOptional.isEmpty() || fuelGradeOptional.isEmpty()) return;

        authorizeIfNoPumpAttendant(pump, nozzleOptional.get(), fuelGradeOptional.get(), tag, stationDto, controllerPtsConfiguration);
    }

    public void authorizeIfNoPumpAttendant(Pump pump, Nozzle nozzle, FuelGrade fuelGrade, String tag, StationDto stationDto, ControllerPtsConfiguration controllerPtsConfiguration) {
        if (pumpAttendantService.findPumpAttandantByStationAndTag(stationDto.getId(), tag).isPresent()) return;

        AuthorizationRequest authorizationRequest = AuthorizationRequest.builder()
                .reference(String.valueOf(stationDto.getCustomerAccountId()))
                .ptsId(controllerPtsConfiguration.getPtsId())
                .pump(pump.getIdConf())
                .tag(tag)
                .productName(fuelGrade.getName())
                .salePointName(stationDto.getName())
                .salePointIdentifier(stationDto.getIdentifier())
                .build();

        AuthorizationDto authorizationDto = customerAccountAuthorization.requestAuthorization(authorizationRequest);

        if (EnumAuthorizationStatus.GRANTED.equals(authorizationDto.getStatus())) {
            authorizationDto.setCustomerAccountReference(String.valueOf(stationDto.getCustomerAccountId()));
            pumpAuthorize(authorizationDto, controllerPtsConfiguration.getPtsId(), pump.getIdConf(), nozzle.getIdConf(), authorizationDto.getQuantity().doubleValue(), fuelGrade.getPrice());
        }
    }

    public void blockCardOnFillingStatus(Pumps pumps, ControllerPtsConfiguration controllerPtsConfiguration) {
        FillingStatus fillingStatus = pumps.getFillingStatus();
        if (fillingStatus == null || fillingStatus.getIds() == null) {
            return; // No filling status to process
        }

        List<Long> pumpIds = fillingStatus.getIds();
        List<Long> transactions = fillingStatus.getTransactions();
        String ptsId = controllerPtsConfiguration.getPtsId();
        // Iterate over the pumps in the FillingStatus
        for (int i = 0; i < pumpIds.size(); i++) {
            Long pumpId = pumpIds.get(i);
            Long transactionId = transactions.get(i);

            // Verify the pump and transaction ID with the given ptsId in the pumpAuthorizationMap
            Map<Long, PumpAuthorizationInfo> pumpMap = pumpAuthorizationMap.get(ptsId);
            if (pumpMap != null) {
                PumpAuthorizationInfo authorizationInfo = pumpMap.get(pumpId);
                if (authorizationInfo != null && transactionId != null && transactionId.equals(authorizationInfo.getTransactionId()) && !authorizationInfo.getStatus().equals(EnumCardStatus.IN_USE)) {
                    // Block the card associated with this transaction
                    authorizationInfo.setStatus(EnumCardStatus.IN_USE);
                    customerAccountAuthorization.updateCardStatus(authorizationInfo.getCardId(),authorizationInfo.getAuthorizationId(),transactionId, EnumCardStatus.IN_USE);
                    log.info("Card with ID {} in use  for Pump {} and Transaction {} under PtsId {}", authorizationInfo.getCardId(), pumpId, transactionId, ptsId);
                }
            }
        }
    }



    public void pumpAuthorize(AuthorizationDto authorizationDto, String ptsId, Long pump, Long nozzle, Double quantity, Double price) {
        String type = "";
        if (authorizationDto.getCardType().equals(EnumCardType.QUANTITY)) {
            type = "Volume";
        } else {
            type = "Amount";
        }

        PumpAuthorizeRequestPacket requestPacket = PumpAuthorizeRequestPacket.builder()
                .id(FIXED_REQUEST_ID)
                .pump(pump)
                .nozzle(nozzle)
                .type(type)
                .dose(quantity)
                .price(price)
                .build();
        PumpAuthorizationInfo authorizationInfo = new PumpAuthorizationInfo(authorizationDto.getId(),authorizationDto.getCardId(), FIXED_REQUEST_ID,null,EnumCardStatus.AUTHORIZED);
        pumpAuthorizationMap.computeIfAbsent(ptsId, k -> new ConcurrentHashMap<>()).put(pump, authorizationInfo);
        essWebSocketHandler.sendRequests(ptsId, List.of(requestPacket));
    }

    @EventListener
    public void handleMessageEvent(WebSocketMessageEvent event) throws JsonProcessingException {
        String message = event.getMessage();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(message);
        String ptsId = root.path("PtsId").asText();
        JsonNode packets = root.path("Packets").get(0);
        JsonNode data = packets.path("Data");
        if (message.contains("PumpAuthorizeConfirmation")) {
            Long responseId = packets.path("Id").longValue();
            Long pumpId = data.path("Pump").longValue();
            Long transactionId = data.path("Transaction").longValue();
            // Parse the JSON message to a PumpAuthorizeConfirmationPacket
            handlePumpAuthorizeConfirmation(ptsId,pumpId,responseId,transactionId);
        }
        if (message.contains("UploadPumpTransaction")) {
            BigDecimal volume = data.path("Volume").decimalValue();
            BigDecimal amount = data.path("Amount").decimalValue();
            Double price = data.path("Price").doubleValue();
            String productName = data.path("FuelGradeName").asText();
            String dateTimeStartStr = data.path("DateTimeStart").asText();
            LocalDateTime dateTimeStart = null;
            if (!dateTimeStartStr.isEmpty()) {
                dateTimeStart = LocalDateTime.parse(dateTimeStartStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            Long pump = data.path("Pump").longValue();
            Long transactionId = data.path("Transaction").longValue();
            Map<Long, PumpAuthorizationInfo> pumpMap = pumpAuthorizationMap.get(ptsId);
            if (pumpMap != null) {
                PumpAuthorizationInfo authorizationInfo = pumpMap.get(pump);
                if (authorizationInfo != null && transactionId != null && transactionId.equals(authorizationInfo.getTransactionId())) {
                    StationDto stationDto = stationDao.findByControllerPtsId(ptsId);
                    CMTransactionDto cmTransactionDto = CMTransactionDto.builder()
                            .reference(String.valueOf(stationDto.getCustomerAccountId()))
                            .authorizationId(authorizationInfo.getAuthorizationId())
                            .cardId(authorizationInfo.getCardId())
                            .dateTime(dateTimeStart)
                            .amount(amount)
                            .price(price)
                            .quantity(volume)
                            .productName(productName)
                            .salePointName(stationDto.getName())
                            .build();
                    customerAccountAuthorization.saveTransaction(cmTransactionDto);
                    authorizationInfo.setStatus(EnumCardStatus.FREE);
                    // Block the card associated with this transaction
                    customerAccountAuthorization.updateCardStatus(authorizationInfo.getCardId(), authorizationInfo.getAuthorizationId(),transactionId, EnumCardStatus.FREE);
                }
            }
        }
    }

    public void handlePumpAuthorizeConfirmation(String ptsId, Long pumpId, Long responseId, Long transactionId) {
        Map<Long, PumpAuthorizationInfo> pumpMap = pumpAuthorizationMap.get(ptsId);
        if (pumpMap != null) {
            PumpAuthorizationInfo authorizationInfo = pumpMap.get(pumpId);
             // Compare with the retrieved id to ensure it's the correct confirmation
                if (authorizationInfo != null && authorizationInfo.getRequestId().equals(responseId)) {
                    // Update the card status to "CONFIRMATION"
                    customerAccountAuthorization.updateCardStatus(authorizationInfo.getCardId(),authorizationInfo.getAuthorizationId(),transactionId, EnumCardStatus.CONFIRMATION);
                    authorizationInfo.setStatus(EnumCardStatus.CONFIRMATION);
                    authorizationInfo.setTransactionId(transactionId);
                }
        }
    }
}
