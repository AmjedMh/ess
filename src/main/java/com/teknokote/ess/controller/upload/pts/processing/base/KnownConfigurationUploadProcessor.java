package com.teknokote.ess.controller.upload.pts.processing.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teknokote.ess.controller.upload.pts.info.UploadInformation;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.exchanges.EnumMessageProcessingState;
import com.teknokote.ess.core.service.impl.ControllerPtsConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Component
public abstract class KnownConfigurationUploadProcessor implements UploadProcessor
{
   @Autowired
   private ControllerPtsConfigurationService controllerPtsConfigurationService;
   @Override
   public void process(Long traceId, String uploadedBody, UploadInformation uploadInformation, BiConsumer<Long, EnumMessageProcessingState> loggingFn) throws JsonProcessingException
   {
      loggingFn.accept(traceId,EnumMessageProcessingState.PROCESSING);
      if(controllerPtsConfigurationService.configurationExists(uploadInformation.getPtsId(), (String) (uploadInformation.getDistinctConfigurationIds().toArray()[0]))) {
         doProcess(uploadedBody, uploadInformation.getIdentifiedControllerPtsConfiguration());
         loggingFn.accept(traceId,EnumMessageProcessingState.SUCCESS);
      }
      else
      {
         loggingFn.accept(traceId,EnumMessageProcessingState.UNKNOWN_CONFIGURATION);
      }
   }

   protected abstract void doProcess(String uploadedBody, ControllerPtsConfiguration controllerPtsConfiguration) throws JsonProcessingException;
}
