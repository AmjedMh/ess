package com.teknokote.ess.controller.upload.pts.processing.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teknokote.ess.controller.upload.pts.info.UploadInformation;
import com.teknokote.ess.core.model.exchanges.EnumMessageProcessingState;

import java.util.function.BiConsumer;

public interface UploadProcessor
{
   String getKey();

   void process(Long traceId, String uploadedBody, UploadInformation uploadInformation, BiConsumer<Long, EnumMessageProcessingState> loggingFn) throws JsonProcessingException;
}
