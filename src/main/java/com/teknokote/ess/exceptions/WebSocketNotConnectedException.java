package com.teknokote.ess.exceptions;

public class WebSocketNotConnectedException extends RuntimeException {
    public WebSocketNotConnectedException() {
        super();
    }

    public WebSocketNotConnectedException(String message) {
        super(message);
    }

    public WebSocketNotConnectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
