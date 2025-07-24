package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.exchanges.EnumMessageOrigin;
import com.teknokote.ess.core.model.exchanges.EnumMessageProcessingState;
import com.teknokote.ess.core.model.exchanges.EnumUploadMessageType;
import com.teknokote.ess.core.model.exchanges.MessagePtsLog;
import com.teknokote.ess.core.repository.MessagePTSLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MessagePTSLogService
{
   @Autowired
   private MessagePTSLogRepository messagePTSLogRepository;
   private static final int MAX_STACK_TRACE_LENGTH = 5000;
   private static final String UNSUPPORTED_VERSION = "UNSUPPORTED VERSION";
   private static final String TRACE_ID = "TraceId introuvable." ;

   @Async
   public void logWSSMsgSuccess(String ptsId, String firmwareVersion, String message)
   {
      messagePTSLogRepository.save(MessagePtsLog.logWSSMsgSuccess(ptsId, EnumMessageOrigin.APP, firmwareVersion, message));
   }
   @Async
   public void logWSSMsgFail(String ptsId, String firmwareVersion, String message)
   {
      messagePTSLogRepository.save(MessagePtsLog.logWSSMsgFail(ptsId, EnumMessageOrigin.APP, firmwareVersion, message));
   }

   public Long logReceivedWSSMsg(String ptsId, String firmwareVersion, String message){
      final MessagePtsLog messagePtsLog = messagePTSLogRepository.save(MessagePtsLog.logWSSMsgAsReceived(ptsId, EnumMessageOrigin.CTRL, firmwareVersion,getDetectedMessageKnownType(message), message));
      return messagePtsLog.getId();
   }
   private EnumUploadMessageType getDetectedMessageKnownType(String message){
      for(EnumUploadMessageType element:EnumUploadMessageType.values()){
         if(message.contains("\"Type\":\""+element.name())){
            return element;
         }
      }
      return null; //Unknown
   }

   public Long logUploadedMsgAsReceived(String ptsId, String firmwareVersion, String configurationId, EnumUploadMessageType messageType, String message)
   {
      MessagePtsLog messagePTSLog=messagePTSLogRepository.save(MessagePtsLog.logUploadedMsgAsReceived(ptsId, EnumMessageOrigin.CTRL, firmwareVersion,configurationId,messageType, message));
      return messagePTSLog.getId();
   }

   /**
    * Echec de traitement d'une trace donnée
    */
   public void logUploadFailedMsgProcessing(Long traceId, Throwable cause)
   {
      MessagePtsLog messagePTSLog = messagePTSLogRepository.findById(traceId).orElseThrow(()->new RuntimeException(TRACE_ID));
      messagePTSLog.setProcessingState(EnumMessageProcessingState.FAIL);
      final String stackTrace = ExceptionUtils.getStackTrace(cause);
      messagePTSLog.setFailReason(stackTrace.substring(0,Math.min(MAX_STACK_TRACE_LENGTH,stackTrace.length())));
      messagePTSLog.setUpdateDate(LocalDateTime.now());
      messagePTSLogRepository.save(messagePTSLog);
   }
   public void logUploadFailedMsgOnUnsupportedVersion(Long traceId)
   {
      MessagePtsLog messagePTSLog = messagePTSLogRepository.findById(traceId).orElseThrow(()->new RuntimeException(TRACE_ID));
      messagePTSLog.setProcessingState(EnumMessageProcessingState.FAIL);
      messagePTSLog.setFailReason(UNSUPPORTED_VERSION);
      messagePTSLog.setUpdateDate(LocalDateTime.now());
      messagePTSLogRepository.save(messagePTSLog);
   }

   /**
    * Le traitement a bien réussi
    */
   public void logUploadMsgSuccess(Long traceId)
   {
      MessagePtsLog messagePTSLog = messagePTSLogRepository.findById(traceId).orElseThrow(()->new RuntimeException(TRACE_ID));
      messagePTSLog.setProcessingState(EnumMessageProcessingState.SUCCESS);
      messagePTSLog.setUpdateDate(LocalDateTime.now());
      messagePTSLogRepository.save(messagePTSLog);
   }

   /**
    * Echec de lecture des informations depuis le JSON reçu initialement
    */
   public void logUploadFailedMsgReadingInformation(String ptsId, String firmwareVersion, String message, Throwable cause)
   {
      MessagePtsLog messagePTSLog = MessagePtsLog.logUploadedMsgAsFailExtractInfo(ptsId, EnumMessageOrigin.CTRL, firmwareVersion, message);
      messagePTSLog.setProcessingState(EnumMessageProcessingState.FAIL_EXTRACT_INFO);
      final String stackTrace = ExceptionUtils.getStackTrace(cause);
      messagePTSLog.setFailReason(stackTrace.substring(0,Math.min(MAX_STACK_TRACE_LENGTH,stackTrace.length())));
      messagePTSLog.setUpdateDate(LocalDateTime.now());
      messagePTSLogRepository.save(messagePTSLog);
   }

   /**
    *
    */
   public void logUploadFailedMsgReadingInformation(String ptsId, String firmwareVersion, String message, String cause,EnumUploadMessageType messageType)
   {
      MessagePtsLog messagePTSLog = MessagePtsLog.logUploadedMsgAsFailExtractInfo(ptsId, EnumMessageOrigin.CTRL, firmwareVersion, message);
      messagePTSLog.setProcessingState(EnumMessageProcessingState.FAIL_EXTRACT_INFO);
      messagePTSLog.setFailReason(cause);
      messagePTSLog.setUpdateDate(LocalDateTime.now());
      messagePTSLog.setMessageType(messageType);
      messagePTSLogRepository.save(messagePTSLog);
   }

   public void logUploadFailedMsgWithUnknownConfigurationId(Long traceId)
   {
      MessagePtsLog messagePTSLog = messagePTSLogRepository.findById(traceId).orElseThrow(()->new RuntimeException(TRACE_ID));
      messagePTSLog.setProcessingState(EnumMessageProcessingState.UNKNOWN_CONFIGURATION);
      messagePTSLog.setUpdateDate(LocalDateTime.now());
      messagePTSLogRepository.save(messagePTSLog);
   }

   /**
    * Permet de passer l'état de processing du message d'id {@param traceId} à {@param state}
    */
   public void logProcessingState(Long traceId,EnumMessageProcessingState state)
   {
      MessagePtsLog messagePTSLog = messagePTSLogRepository.findById(traceId).orElseThrow(()->new RuntimeException(TRACE_ID));
      messagePTSLog.setProcessingState(state);
      messagePTSLog.setUpdateDate(LocalDateTime.now());
      messagePTSLogRepository.save(messagePTSLog);
   }

   /**
    * Renvoie la liste des Ids des messages échoués
    * @return List<Long>
    */
   public List<Long> findFailedTransactionsId(String ptsId, LocalDateTime startDate,LocalDateTime endDate)
   {
      return messagePTSLogRepository.findFailedTransactions(ptsId, EnumMessageOrigin.CTRL,startDate,endDate);
   }

   public List<MessagePtsLog> findMeasurementByMessageTypeAndDateRange(LocalDateTime startDate, LocalDateTime endDate,String ptsId)
   {
      return messagePTSLogRepository.findMeasurementByMessageTypeAndDateRange(startDate,endDate, ptsId);
   }

   public Optional<MessagePtsLog> findById(Long id)
   {
      return messagePTSLogRepository.findById(id);
   }

   public List<MessagePtsLog> findMessagesByDate(LocalDateTime startDate, LocalDateTime endDate, String ptsId) {
      return messagePTSLogRepository.findMessagesByDate(startDate,endDate,ptsId);
   }
}
