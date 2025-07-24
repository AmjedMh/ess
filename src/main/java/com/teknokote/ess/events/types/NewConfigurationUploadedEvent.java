package com.teknokote.ess.events.types;

import com.teknokote.pts.client.upload.configuration.UploadConfigRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

@Getter
@Setter
public class NewConfigurationUploadedEvent extends ApplicationEvent
{
   @Serial
   private static final long serialVersionUID = 7499676661934894474L;
   private final transient UploadConfigRequest uploadConfigRequest;
   private final String ptsId;
   public NewConfigurationUploadedEvent(Object source, UploadConfigRequest uploadConfigRequest, String ptsId)
   {
      super(source);
      this.uploadConfigRequest=uploadConfigRequest;
      this.ptsId=ptsId;
   }
   public static NewConfigurationUploadedEvent of(Object source, UploadConfigRequest uploadConfigRequest, String ptsId)
   {
      return new NewConfigurationUploadedEvent(source,uploadConfigRequest,ptsId);
   }
}
