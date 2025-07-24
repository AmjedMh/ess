package com.teknokote.ess.integration.unit.uploads;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.pts.client.upload.dto.MeasurementDto;
import com.teknokote.pts.client.upload.status.UploadStatusRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static com.teknokote.ess.integration.TestUtils.readFile;
 class UploadStatusTest
{
   @Test
   void loadUploadStatus() throws IOException
   {
      UploadStatusRequest query = new ObjectMapper().readValue(readFile("/uploads/upload-status.json"), UploadStatusRequest.class);
      final Map<Long, MeasurementDto> probeMeasurements = query.getOnlineProbeMeasurementsOnDefaultPacket();
      Assertions.assertNotNull(probeMeasurements);
      Assertions.assertEquals(3,probeMeasurements.size());
   }
}
