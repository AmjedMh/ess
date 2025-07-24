package com.teknokote.ess.events.listeners;

import org.springframework.context.ApplicationEvent;

public class WebSocketMessageEvent extends ApplicationEvent {
    private final String message;

    public WebSocketMessageEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
