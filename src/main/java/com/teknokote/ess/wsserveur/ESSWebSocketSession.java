package com.teknokote.ess.wsserveur;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Data
@Builder
public class ESSWebSocketSession
{
   private WebSocketSession session;
   private String ptsId;
   private String configurationId;
   private String firmwareVersion;
   private boolean firmwareVersionSupported;

   public void sendMessage(String message) throws IOException
   {
      session.sendMessage(new TextMessage(message));
   }

   public boolean isOpen()
   {
      return session.isOpen();
   }

   public boolean isActiveConnection(){
      return StringUtils.hasText(ptsId) && isOpen() && isFirmwareVersionSupported();
   }
}
