package com.teknokote.ess.controller.upload.pts.processing.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.controller.upload.pts.info.UploadInformation;
import com.teknokote.ess.controller.upload.pts.info.UploadPacketInformation;
import com.teknokote.ess.controller.upload.pts.processing.UploadProcessorSwitcher;
import com.teknokote.ess.controller.upload.pts.validation.EnumValidationResult;
import com.teknokote.ess.core.model.exchanges.MessagePtsLog;
import com.teknokote.ess.core.service.impl.ControllerPtsConfigurationService;
import com.teknokote.ess.core.service.impl.ControllerService;
import com.teknokote.ess.core.service.impl.FirmwareInformationService;
import com.teknokote.ess.core.service.impl.MessagePTSLogService;
import com.teknokote.ess.dto.SearchTransactionDto;
import com.teknokote.ess.utils.EssUtils;
import com.teknokote.pts.client.upload.ConfirmationResponsePacket;
import com.teknokote.pts.client.upload.PTSConfirmationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UploadProcessorSwitcherImpl implements UploadProcessorSwitcher
{
   @Autowired
   private MessagePTSLogService messagePTSLogService;
   @Autowired
   private FirmwareInformationService firmwareInformationService;
   @Autowired
   private ControllerService controllerService;
   @Autowired
   private ControllerPtsConfigurationService controllerPtsConfigurationService;
   @Autowired
   private List<UploadProcessor> uploadProcessors;
   private Map<String, UploadProcessor> mapOfUploadProcessors(){
      return uploadProcessors.stream().collect(Collectors.toMap(UploadProcessor::getKey, Function.identity()));
   }

   /**
    * Méthode principale de traitement d'un message reçu du contrôleur à appeler par les clients
    * TODO A simplifier: solution rapide
    */
   @Override
   public PTSConfirmationResponse process(String uploadedBody){
      return process(uploadedBody,null,true);
   }

   @Override
   public PTSConfirmationResponse process(String uploadedBody, Long inTraceId,boolean firmwareVersionSupported)
   {
      //TODO Rajouter le cas de parse error: on doit renvoyer confirmation error.
      final UploadInformation uploadInformation = readUploadInformation(uploadedBody)
         .completeControllerInfo(controllerService::findController,controllerPtsConfigurationService::findByPtsIdAndConfigurationId,firmwareInformationService::firmwareVersion);
      final List<EnumValidationResult> validationResults = uploadInformation.validate();
      if(!validationResults.isEmpty()) {
         final String identifiedPtsId = EssUtils.extractPtsId(uploadedBody);
         messagePTSLogService.logUploadFailedMsgReadingInformation(identifiedPtsId, uploadInformation.getIdentifiedVersion(),
            uploadedBody, validationResults.stream().map(Enum::name).collect(Collectors.joining(",")),uploadInformation.getDetectedKnownMessageType());
         return createConfirmationFromUploadInformation(uploadInformation, JSONPTS_ERROR_NOT_FOUND, true);
      }
      long traceId=Objects.isNull(inTraceId)? logReceivedMessage(uploadedBody,uploadInformation):inTraceId;
      try
      {
         String firmwareVersion = uploadInformation.getIdentifiedVersion();
         if (!firmwareVersionSupported) {
            log.warn("Unsupported firmware version: {}", firmwareVersion);
            messagePTSLogService.logUploadFailedMsgOnUnsupportedVersion(traceId);
            return createConfirmationFromUploadInformation(uploadInformation, "Firmware not supported", true);
         }
         if (!uploadInformation.isExpectedFormat()){
            return createConfirmationFromUploadInformation(uploadInformation, OK_MESSAGE, true);
         }

         // Control number of configurations
         final Set<String> distinctConfigurationIds = uploadInformation.getDistinctConfigurationIds();
         if(distinctConfigurationIds.size()>1) throw new RuntimeException("Cas inattendu: plusieures configurations renvoyées dans un seul upload configuration!");
         if(distinctConfigurationIds.isEmpty()) throw new RuntimeException("Cas inattendu: pas de configuration id dans un upload configuration!");
         // Launch process
         mapOfUploadProcessors().get(uploadInformation.getUploadPacketInformations().get(0).getType()).process(traceId,uploadedBody,uploadInformation, messagePTSLogService::logProcessingState);
      }
      catch(Exception exception)
      {
         messagePTSLogService.logUploadFailedMsgProcessing(traceId, exception);
      }
      return createConfirmationFromUploadInformation(uploadInformation, OK_MESSAGE, false);
   }

   private long logReceivedMessage(String uploadedBody, UploadInformation uploadInformation)
   {
      return messagePTSLogService.logUploadedMsgAsReceived(uploadInformation.getPtsId(), uploadInformation.getIdentifiedVersion(),
         String.join(",", uploadInformation.getDistinctConfigurationIds()),uploadInformation.getDetectedKnownMessageType(), uploadedBody);
   }

   /**
    * Lit les informations d'Upload reçu depuis le contrôleur
    */
   private UploadInformation readUploadInformation(String uploadedBody)
   {
      UploadInformation uploadInformation;
      try
      {
         uploadInformation = new ObjectMapper().readValue(uploadedBody, UploadInformation.class);
      }
      catch(JsonProcessingException jsonProcessingException) {
         final String identifiedPtsId = EssUtils.extractPtsId(uploadedBody);
         final String packetId = EssUtils.extractPacketId(uploadedBody);
         final String packetType = EssUtils.extractType(uploadedBody);
         String configurationId = controllerPtsConfigurationService.findCurrentConfigurationIdByPtsId(identifiedPtsId);
         uploadInformation =  new UploadInformation();
         uploadInformation.setPtsId(identifiedPtsId);
         uploadInformation.setExpectedFormat(false);
         UploadInformation.UploadPacketInformation uploadPacketInformation = new UploadInformation.UploadPacketInformation();
         uploadPacketInformation.setId(packetId);
         uploadPacketInformation.setType(packetType);
         uploadPacketInformation.setConfigurationId(configurationId);
         uploadInformation.setUploadPacketInformations(List.of(uploadPacketInformation));
      }
      return uploadInformation;
   }

   /**
    * Prépare une réponse de confirmation au contrôleur.
    * si error = false, message doit contenir MESSAGE_OK
    * si error = true, message contient le motif de l'erreur.
    */
   protected PTSConfirmationResponse createConfirmationFromUploadInformation(UploadInformation uploadInformation, String message, boolean error){
      final List<ConfirmationResponsePacket> confirmationResponsePackets =
         uploadInformation.getUploadPacketInformations().stream().map(el ->
         {
            final ConfirmationResponsePacket.ConfirmationResponsePacketBuilder packetBuilder = ConfirmationResponsePacket.builder()
               .id(Long.valueOf(el.getId()))
               .type(el.getType())
               .message(message)
               .getConfiguration(!controllerPtsConfigurationService.configurationExists(uploadInformation.getPtsId(), el.getConfigurationId()));
            if(error)packetBuilder.error(error);
            return packetBuilder.build();
         }).collect(Collectors.toList());
      return PTSConfirmationResponse.builder().ptsId(uploadInformation.getPtsId()).packets(confirmationResponsePackets).build();
   }
   @Override
   public void processUploads(SearchTransactionDto dto) {
      validateSearchTransactionDto(dto);

      log.info("[UPLOAD PROCESS] Start processing {} for criteria: {}", dto.getUploadType(), dto);

      if(dto.getUploadType().equals(SearchTransactionDto.UploadType.TRANSACTIONS)) {
         processFailedUploadTransactions(dto);
      }
      else{
            resendMessagesByTypeAndDateRange(dto.getStartDate(), dto.getEndDate(), dto.getPtsId(), dto.getUploadType());
      }
   }

   private void validateSearchTransactionDto(SearchTransactionDto dto) {
      if (!StringUtils.hasText(dto.getPtsId())) {
         throw new BadRequestException("PtsId cannot be empty");
      }
      if (Objects.isNull(dto.getEndDate()) || dto.getEndDate().isBefore(dto.getStartDate())) {
         throw new BadRequestException("End date cannot be before start date");
      }
   }
   public void processFailedUploadTransactions(SearchTransactionDto searchTransactionDto){
      log.info("[FAILED TRANSACTIONS] Start processing for criteria:{}", searchTransactionDto);
      LocalDateTime startDate = searchTransactionDto.getStartDate();
      LocalDateTime endDate = searchTransactionDto.getEndDate();
         final List<Long> failedTransactionsId = messagePTSLogService.findFailedTransactionsId(searchTransactionDto.getPtsId(),startDate,endDate);
         log.info("[FAILED TRANSACTIONS] {} transactions to process for period between {} and {}.",failedTransactionsId.size(),startDate,endDate);
         final int sum = failedTransactionsId.stream().mapToInt(tr -> processFailedTransaction(tr, searchTransactionDto.getPumpId())).sum();
         log.info("[FAILED TRANSACTIONS] End processing. {} transactions processed.", sum);
   }

   private int processFailedTransaction(Long messagePtsLogId,Long pumpId) {
      AtomicInteger processed=new AtomicInteger(0);
      messagePTSLogService.findById(messagePtsLogId).ifPresent(msg -> {
         if (msg.getMessage().contains("\"Type\":\"UploadPumpTransaction\"") && (Objects.isNull(pumpId) || msg.getMessage().contains("\"Pump\":" + pumpId))) {
            process(msg.getMessage());
            processed.incrementAndGet();
         }
      });
      return processed.get();
   }

   public void resendMessagesByTypeAndDateRange(LocalDateTime startDate, LocalDateTime endDate, String ptsId, SearchTransactionDto.UploadType uploadType) {
      // Log the start of the resend operation
      log.info("[RESEND MESSAGES] Start resending messages of type 'UploadTankMeasurement' between {} and {}.", startDate, endDate);
      List<MessagePtsLog> logs;
      // Fetch the messages from the log based on the given date range and type
      if (uploadType.equals(SearchTransactionDto.UploadType.TANK_MEASUREMENTS)) {
         logs = messagePTSLogService.findMeasurementByMessageTypeAndDateRange(startDate, endDate, ptsId);
      }else{
         logs = messagePTSLogService.findMessagesByDate(startDate, endDate, ptsId);
      }
      // Check if no messages are found to resend
      if (logs.isEmpty()) {
         log.info("[RESEND MESSAGES] No messages found to resend.");
         return;
      }

      // Iterate through each log entry and attempt to resend the message
      logs.forEach(logEntry -> {
         try {
            // Log the ID of the message being resent
            log.info("[RESEND MESSAGES] Resending message with ID {}.", logEntry.getId());

            // Process and resend the message
            process(logEntry.getMessage());

            // Log successful message resend
            log.info("[RESEND MESSAGES] Successfully resent message with ID {}.", logEntry.getId());
         } catch (Exception e) {
            // Log error details if message resending fails
            log.error("[RESEND MESSAGES] Failed to resend message with ID {}. Error: {}", logEntry.getId(), e.getMessage(), e);

            // Log the failed message processing for future tracking
            messagePTSLogService.logUploadFailedMsgProcessing(logEntry.getId(), e);
         }
      });

      // Log completion of the resend process
      log.info("[RESEND MESSAGES] Finished resending {} messages.", logs.size());
   }
}
