package com.teknokote.ess.events.publish.cm;

import com.teknokote.ess.events.publish.cm.exception.ModuleException;
import com.teknokote.ess.events.publish.cm.exception.ModuleNotFoundException;
import com.teknokote.ess.events.publish.cm.exception.ModuleServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CardManagerErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response != null && response.body() != null) {
            try {
                String responseBody = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);

                if (response.status() == HttpStatus.NOT_FOUND.value()) {
                    return new ModuleNotFoundException("Resource not found: " + responseBody);
                } else if (response.status() == HttpStatus.BAD_REQUEST.value()) {
                    return new ModuleServiceException(responseBody);
                } else {
                    return new ModuleException("An error occurred: " + responseBody);
                }
            } catch (IOException e) {
                return new ModuleServiceException("Error decoding response: " + e.getMessage());
            }
        }
        return null;
    }
}
