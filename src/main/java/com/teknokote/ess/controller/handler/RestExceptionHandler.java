package com.teknokote.ess.controller.handler;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.exceptions.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> entityNotFound(EntityNotFoundException exception) {
        return badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<String> unexceptedException(UnexpectedException exception) {
        return badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(ServiceValidationException.class)
    public ResponseEntity<String> serviceValidationException(ServiceValidationException exception) {
        return badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(KeycloakUserCreationException.class)
    public ResponseEntity<String> serviceValidationException(KeycloakUserCreationException exception) {
        return ok(exception.getMessage());
    }

    @ExceptionHandler(ShiftExecutionException.class)
    public ResponseEntity<String> serviceValidationException(ShiftExecutionException exception) {
        return ok(exception.getMessage());
    }

    @ExceptionHandler(WebSocketNotConnectedException.class)
    public ResponseEntity<String> webSocketException(WebSocketNotConnectedException exception) {

        return badRequest().body(exception.getMessage());
    }
}
