package com.teknokote.ess.events.publish.cm.exception;

import com.teknokote.core.exceptions.ServiceValidationException;

public class ExceptionHandlerUtil {
    private ExceptionHandlerUtil() {
    }

    public static void handleException(Exception e, String moduleName) {
        String errorMessage;

        if (e instanceof ModuleNotFoundException) {
            errorMessage = moduleName + " resource not found: " + e.getMessage();
        } else if (e instanceof ModuleServiceException) {
            errorMessage = moduleName + " service error: " + e.getMessage();
        } else if (e instanceof ModuleException) {
            errorMessage = moduleName + " error: " + e.getMessage();
        } else {
            errorMessage = moduleName + " service unavailable: ";
        }

        throw new ServiceValidationException(errorMessage);
    }
}

