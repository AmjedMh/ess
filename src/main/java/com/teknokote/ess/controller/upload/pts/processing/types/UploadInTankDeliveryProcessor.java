package com.teknokote.ess.controller.upload.pts.processing.types;

import com.teknokote.ess.controller.upload.pts.processing.base.KnownConfigurationUploadProcessor;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import org.springframework.stereotype.Component;

@Component
public class UploadInTankDeliveryProcessor extends KnownConfigurationUploadProcessor {

    @Override
    protected void doProcess(String uploadedBody, ControllerPtsConfiguration controllerPtsConfiguration) {
    // This method is intentionally left unimplemented because the logic for processing
    // "UploadInTankDelivery" will be handled by a different processor.
    // Throwing an exception ensures that any unintended calls are flagged during development.
    }

    @Override
    public String getKey() {
        return "UploadInTankDelivery";
    }
}
