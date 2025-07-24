package com.teknokote.ess.controller.upload.pts.info;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UploadInformationExtractor
{
   public UploadInformation getUploadInformation(String content) throws JsonProcessingException
   {
      return new ObjectMapper().readValue(content, UploadInformation.class);
   }
}
