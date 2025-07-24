package com.teknokote.ess.controller.upload.pts.processing.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.controller.upload.pts.processing.base.UnknownConfigurationUploadProcessor;
import com.teknokote.ess.events.types.NewConfigurationUploadedEvent;
import com.teknokote.pts.client.upload.configuration.UploadConfigRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class UploadConfigurationProcessor extends UnknownConfigurationUploadProcessor
{
   @Autowired
   private MergeConfigurationUploads configService;
   @Autowired
   private ApplicationEventPublisher applicationEventPublisher;

   @Override
   protected void doProcess(String uploadedBody, String ptsId) throws JsonProcessingException
   {
      UploadConfigRequest uploadConfigRequest = new ObjectMapper().readValue(uploadedBody, UploadConfigRequest.class);
      configService.uploadConfig(uploadConfigRequest, ptsId);
      applicationEventPublisher.publishEvent(NewConfigurationUploadedEvent.of(this,uploadConfigRequest,ptsId));
   }

   @Override
   public String getKey()
   {
      return "UploadConfiguration";
   }
}
