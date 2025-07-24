package com.teknokote.ess.controller.upload.pts;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.controller.upload.pts.confirmation.PrepareConfirmationResponse;
import com.teknokote.ess.controller.upload.pts.processing.UploadProcessorSwitcher;
import com.teknokote.pts.client.upload.PTSConfirmationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(EndPoints.UPLOAD)
@Slf4j
public class ControllerUpload {
   @Autowired
   private PrepareConfirmationResponse prepareConfirmationResponse;

   @Autowired
   private UploadProcessorSwitcher uploadProessorSwitcher;

   @PostMapping(EndPoints.UPLOAD_PUMP_TRANSACTION)
   public ResponseEntity<PTSConfirmationResponse> uploadPumpTransactions(@RequestBody String requestBody) throws IOException {
      return prepareConfirmationResponse.createResponse(uploadProessorSwitcher.process(requestBody));
   }

   @PostMapping(EndPoints.UPLOAD_TANK_MEASUREMENT)
   public ResponseEntity<PTSConfirmationResponse> uploadTankMeasurement(@RequestBody String requestBody) throws IOException {
      return prepareConfirmationResponse.createResponse(uploadProessorSwitcher.process(requestBody));
   }

   @PostMapping(EndPoints.UPLOAD_CONFIG)
   public ResponseEntity<PTSConfirmationResponse> uploadConfiguration(@RequestBody String requestBody) throws IOException {
      return prepareConfirmationResponse.createResponse(uploadProessorSwitcher.process(requestBody));
   }

   @PostMapping(EndPoints.UPLOAD_IN_TANK_DELIVERY)
   public ResponseEntity<PTSConfirmationResponse> uploadInTankDelivery(@RequestBody String requestBody) throws IOException {
      return prepareConfirmationResponse.createResponse(uploadProessorSwitcher.process(requestBody));
   }

   @PostMapping(EndPoints.UPLOAD_STATUS)
   public ResponseEntity<PTSConfirmationResponse> uploadStatus(@RequestBody String requestBody) throws IOException {
      return prepareConfirmationResponse.createResponse(uploadProessorSwitcher.process(requestBody));
   }
   @PostMapping(EndPoints.UPLOAD_ALERT_RECORD)
   public ResponseEntity<PTSConfirmationResponse> uploadAlertRecord(@RequestBody String requestBody) throws IOException {
      return prepareConfirmationResponse.createResponse(uploadProessorSwitcher.process(requestBody));
   }
}
