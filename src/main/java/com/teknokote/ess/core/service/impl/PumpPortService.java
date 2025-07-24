package com.teknokote.ess.core.service.impl;


import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.PumpPort;
import com.teknokote.ess.core.repository.PumpPortRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PumpPortService extends AbstractEntityService<PumpPort, Long>
{

    private PumpPortRepository pumpPortRepository;

    public PumpPortService(PumpPortRepository dao) {
        super(dao);
        this.pumpPortRepository = dao;
    }

    public PumpPort findAllByIdConfiguredAndControllerPtsConfiguration(Long idConf, ControllerPtsConfiguration controllerPtsConfiguration){
        return pumpPortRepository.findAllByIdConfiguredAndControllerPtsConfiguration(idConf,controllerPtsConfiguration).orElse(null);
    }
}
