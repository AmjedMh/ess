package com.teknokote.ess.wsserveur;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer
{
    @Autowired
    private ESSWebSocketHandler webSocketHandler;
    @Autowired
    private WebSocketFrontHandler webSocketFrontHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(webSocketHandler, "/websocket").setAllowedOrigins("*");

        registry.addHandler(webSocketFrontHandler, "/websocketFrontEnd").setAllowedOrigins("*");

    }

}


