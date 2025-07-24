package com.teknokote.ess.events.types;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.pts.client.upload.status.UploadStatusRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * Evènement généré à la réception d'un upload status avec présence d'un tag "Tags".
 */
@Getter
@Setter
public class CardOnReaderEvent extends ApplicationEvent
{
   @Serial
   private static final long serialVersionUID = -1792532199818096043L;
   private final transient UploadStatusRequest uploadStatusRequest;
   private final ControllerPtsConfiguration controllerPtsConfiguration;

   private CardOnReaderEvent(Object source, UploadStatusRequest uploadStatusRequest, ControllerPtsConfiguration controllerPtsConfiguration)
   {
      super(source);
      this.uploadStatusRequest=uploadStatusRequest;
      this.controllerPtsConfiguration=controllerPtsConfiguration;
   }

   public static CardOnReaderEvent of(Object source, UploadStatusRequest anUploadStatusRequest,ControllerPtsConfiguration controllerPtsConfiguration)
   {
      return new CardOnReaderEvent(source,anUploadStatusRequest,controllerPtsConfiguration);
   }
}
