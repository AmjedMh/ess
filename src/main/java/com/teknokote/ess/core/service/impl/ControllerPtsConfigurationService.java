package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.repository.configuration.ControllerPtsConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ControllerPtsConfigurationService {
    @Autowired
    private ControllerPtsConfigurationRepository controllerPtsConfigurationRepository;

    public boolean configurationExists(String ptsId, String configurationId){
        return controllerPtsConfigurationRepository.findByPtsIdAndConfigurationId(ptsId, configurationId).isPresent();
    }

    /**
     * Renvoie la configuration du contrôleur avec {@param ptsId} et {@param configurationId}
     */
    public ControllerPtsConfiguration findByPtsIdAndConfigurationId(String ptsId, String configurationId){
        return controllerPtsConfigurationRepository.findByPtsIdAndConfigurationId(ptsId, configurationId).orElse(null);
    }

    public String findCurrentConfigurationIdByPtsId(String ptsId){
        return controllerPtsConfigurationRepository.findCurrentConfigurationByPtsId(ptsId);
    }

    /**
     * Renvoie la configuration actuelle liée au contrôleur {@param controllerId}
     */
    public ControllerPtsConfiguration findCurrentConfigurationOnController(Long controllerId){
        return controllerPtsConfigurationRepository.findCurrentConfigurationOnController(controllerId).orElse(null);
    }

    public List<ControllerPtsConfiguration> findAll(){
        return controllerPtsConfigurationRepository.findAll();
    }

    public Optional<ControllerPtsConfiguration> findById(Long currentConfigurationId)
    {

        return controllerPtsConfigurationRepository.findById(currentConfigurationId);
    }
}
