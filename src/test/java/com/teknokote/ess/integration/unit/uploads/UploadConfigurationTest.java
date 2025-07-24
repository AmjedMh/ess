package com.teknokote.ess.integration.unit.uploads;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.pts.client.response.ResponsePacket;
import com.teknokote.pts.client.upload.configuration.UploadConfigRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.teknokote.ess.integration.TestUtils.readFile;
 class UploadConfigurationTest
{
   @Test
   void loadUploadConfig() throws IOException
   {
      final UploadConfigRequest query = new ObjectMapper().readValue(readFile("/uploads/upload-configuration-20241112.json"), UploadConfigRequest.class);
      final List<ResponsePacket> configurations = query.getPackets().get(0).getConfiguration();
      Assertions.assertNotNull(configurations);
   }
}
