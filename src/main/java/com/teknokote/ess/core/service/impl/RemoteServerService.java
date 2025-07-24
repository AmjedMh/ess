package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.RemoteServerConf;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.repository.RemoteServerRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import com.teknokote.pts.client.response.JsonPTSResponse;
import org.springframework.stereotype.Service;

@Service
public class RemoteServerService extends AbstractEntityService<RemoteServerConf, Long>
{

    public RemoteServerService(RemoteServerRepository dao) {
        super(dao);
    }

    public JsonPTSResponse addNewRemote(ControllerPtsConfiguration controllerPtsConfiguration, JsonPTSResponse response) {

        return response;
    }
}
