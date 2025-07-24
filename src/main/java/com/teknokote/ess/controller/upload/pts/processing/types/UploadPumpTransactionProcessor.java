package com.teknokote.ess.controller.upload.pts.processing.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.controller.upload.pts.processing.base.KnownConfigurationUploadProcessor;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.pts.client.upload.pump.JsonPumpUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UploadPumpTransactionProcessor extends KnownConfigurationUploadProcessor
{
   @Autowired
   private TransactionService transactionService;
   /**
    * Traite le cas {@param uploadInformation} (Type=UploadTransaction).
    */
   @Override
   protected void doProcess(java.lang.String uploadedBody, ControllerPtsConfiguration controllerPtsConfiguration) throws JsonProcessingException {
      JsonPumpUpload pumpUpload = new ObjectMapper().readValue(uploadedBody, JsonPumpUpload.class);
      transactionService.processUploadedTransaction(pumpUpload, controllerPtsConfiguration);
   }

   @Override
   public String getKey()
   {
      return "UploadPumpTransaction";
   }
}
