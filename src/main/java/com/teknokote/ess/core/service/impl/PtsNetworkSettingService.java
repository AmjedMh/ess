package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.PtsNetwork;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.repository.PtsNetworkSettingRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import com.teknokote.pts.client.response.JsonPTSResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PtsNetworkSettingService extends AbstractEntityService<PtsNetwork, Long>
{

    private PtsNetworkSettingRepository ptsNetworkSettingRepository;

    public PtsNetworkSettingService(PtsNetworkSettingRepository dao) {
        super(dao);
        this.ptsNetworkSettingRepository = dao;
    }

    public List<PtsNetwork> getAll() {

        return ptsNetworkSettingRepository.findAll();
    }

    public JsonPTSResponse addNewNetwork(ControllerPtsConfiguration controllerPtsConfiguration, JsonPTSResponse response) {

        return response;
    }

}
