package com.teknokote.ess.controller.upload.pts.confirmation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teknokote.ess.controller.upload.pts.info.UploadInformation;
import com.teknokote.pts.client.upload.ConfirmationResponsePacket;
import com.teknokote.pts.client.upload.PTSConfirmationResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PrepareConfirmationResponse {

    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";
    public static final String HEADER_CONNECTION_CLOSE = "close";

    public ResponseEntity<PTSConfirmationResponse> responseWithOkMessage(List<ConfirmationResponsePacket> packets) throws JsonProcessingException {
        final PTSConfirmationResponse response = PTSConfirmationResponse.builder().packets(packets).build();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8);
        responseHeaders.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(new ObjectMapper().writeValueAsString(response).length()));
        responseHeaders.add(HttpHeaders.CONNECTION, HEADER_CONNECTION_CLOSE);
        return ResponseEntity.ok().headers(responseHeaders).body(response);
    }

    public ResponseEntity<PTSConfirmationResponse> createResponse(PTSConfirmationResponse confirmationResponse) throws JsonProcessingException {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8);
        responseHeaders.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(new ObjectMapper().writeValueAsString(confirmationResponse).length()));
        responseHeaders.add(HttpHeaders.CONNECTION, HEADER_CONNECTION_CLOSE);
        return ResponseEntity.ok().headers(responseHeaders).body(confirmationResponse);
    }

    /**
     * Créée une réponse à partir de UploadInformation
     */
    public ResponseEntity<PTSConfirmationResponse> createResponse(UploadInformation uploadInformation) throws JsonProcessingException
    {
        final PTSConfirmationResponse response = createFromUploadInformation(uploadInformation);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8);
        responseHeaders.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(new ObjectMapper().writeValueAsString(response).length()));
        responseHeaders.add(HttpHeaders.CONNECTION, HEADER_CONNECTION_CLOSE);
        return ResponseEntity.ok().headers(responseHeaders).body(response);
    }
    public PTSConfirmationResponse createFromUploadInformation(UploadInformation uploadInformation){
        final List<ConfirmationResponsePacket> confirmationResponsePackets =
           uploadInformation.getUploadPacketInformations().stream().map(el ->
              ConfirmationResponsePacket.builder()
                 .id(Long.valueOf(el.getId()))
                 .type(el.getType())
                 .message("OK")
                 .getConfiguration(Objects.isNull(uploadInformation.getIdentifiedControllerPtsConfiguration()))
                 .build()).toList();
        return PTSConfirmationResponse.builder().ptsId(uploadInformation.getPtsId()).packets(confirmationResponsePackets).build();
    }

    public ResponseEntity<PTSConfirmationResponse> responseWithError6(Optional<Long> id, Optional<String> type) throws JsonProcessingException {
        if (!type.isPresent()) {
            // Handle the case when type is not present.
            return ResponseEntity.badRequest().build();
        }

        final ConfirmationResponsePacket packet = ConfirmationResponsePacket.builder()
                .id(id.orElse(0L))
                .type(type.get())
                .error(true)
                .code(6L)
                .message("JSONPTS_ERROR_NO_PERMISSIONS")
                .build();
        return responseWithOkMessage(List.of(packet));
    }

    public ResponseEntity<PTSConfirmationResponse> responseWithError46(Optional<Long> id, Optional<String> type) throws JsonProcessingException {
        if (!type.isPresent()) {
            // Handle the case when type is not present.
            return ResponseEntity.badRequest().build();
        }

        final ConfirmationResponsePacket packet = ConfirmationResponsePacket.builder()
                .id(id.orElse(0L))
                .type(type.get())
                .error(true)
                .code(46L)
                .message("JSONPTS_ERROR_46")
                .build();

        return responseWithOkMessage(List.of(packet));
    }
}
