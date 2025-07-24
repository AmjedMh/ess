package com.teknokote.ess.controller.upload.pts.processing;

import com.teknokote.ess.dto.SearchTransactionDto;
import com.teknokote.pts.client.upload.PTSConfirmationResponse;

public interface UploadProcessorSwitcher
{
   String JSONPTS_ERROR_NOT_FOUND = "JSONPTS_ERROR_NOT_FOUND";
   String OK_MESSAGE = "OK";
   /**
    * Méthode principale de traitement d'un message reçu du contrôleur à appeler par les clients
    */
   PTSConfirmationResponse process(String uploadedBody);

   /**
    * Méthode principale de traitement d'un message reçu du contrôleur à appeler par les clients
    * avec un id de trace à suivre (utiliser dans les logs)
    */
   PTSConfirmationResponse process(String uploadedBody, Long inTraceId,boolean firmwareVersionSupported);

   /**
    * This method processes failed uploads.
    */
   void processUploads(SearchTransactionDto searchTransactionDto);
}
