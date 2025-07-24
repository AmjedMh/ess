package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.configuration.PumpPort;
import com.teknokote.ess.core.repository.PumpPortRepository;
import com.teknokote.ess.core.repository.PumpRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import com.teknokote.ess.dto.PumpsConfigDto;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSPump;
import com.teknokote.pts.client.response.configuration.PTSPumpPort;
import com.teknokote.pts.client.response.configuration.PTSPumpsConfigurationResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSPumpsConfigurationResponsePacketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class PumpService extends AbstractEntityService<Pump, Long> {
    private PumpRepository pumpRepository;
    @Autowired
    private PumpPortRepository pumpPortRepository;
    @Autowired
    private PumpPortService pumpPortService;
    @Autowired
    private ControllerPtsConfigurationService controllerPtsConfigurationService;

    public PumpService(PumpRepository dao,PumpPortService pumpPortService,ControllerPtsConfigurationService controllerPtsConfigurationService, PumpPortRepository pumpPortRepository) {
        super(dao);
        pumpRepository = dao;
        this.pumpPortService = pumpPortService;
        this.controllerPtsConfigurationService = controllerPtsConfigurationService;
        this.pumpPortRepository = pumpPortRepository;
    }

    /**
     * Renvoie la liste des pompes de la dernière configuration du contrôleur identifié par {@param idCtr}
     */
    public List<Pump> findPumpsByControllerOnCurrentConfiguration(Long idCtr) {

        final ControllerPtsConfiguration currentConfigurationOnController = controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr);
        return pumpRepository.findPumpsByControllerConfiguration(currentConfigurationOnController);
    }

    public void addNewPumpPortC(ControllerPtsConfiguration controllerPtsConfiguration, JsonPTSResponse response) {
        response.getPackets().stream()
                .map(responsePacket -> ((PTSPumpsConfigurationResponsePacket) responsePacket).getData())
                .map(PTSPumpsConfigurationResponsePacketData::getPorts)
                .flatMap(Collection::stream)
                .forEach(port -> addPort(port, controllerPtsConfiguration));

        response.getPackets().stream()
                .map(responsePacket -> ((PTSPumpsConfigurationResponsePacket) responsePacket).getData())
                .map(PTSPumpsConfigurationResponsePacketData::getPumps)
                .flatMap(Collection::stream)
                .map(pump -> addPump(pump, controllerPtsConfiguration))
                .forEach(pump -> {});

    }

    public void addPort(PTSPumpPort port, ControllerPtsConfiguration controllerPtsConfiguration) {

        Optional<PumpPort> p = pumpPortRepository.findAllByIdConfiguredAndControllerPtsConfiguration(port.getId(), controllerPtsConfiguration);
        if (p.isEmpty())
            p = Optional.of(new PumpPort());
        PumpPort pumpPort = p.get();
        pumpPort.setIdConfigured(port.getId());
        pumpPort.setProtocol(port.getProtocol());
        pumpPort.setBaudRate(port.getBaudRate());
        pumpPort.setControllerPtsConfiguration(controllerPtsConfiguration);
        pumpPortRepository.save(pumpPort);
    }

    public Pump addPump(PTSPump pump, ControllerPtsConfiguration controllerPtsConfiguration) {
        Pump existingOrNewPump = pumpRepository.findAllByIdConfAndControllerPtsConfiguration(pump.getId(), controllerPtsConfiguration);
        if (existingOrNewPump == null)
            existingOrNewPump = new Pump();
        existingOrNewPump.setIdConf(pump.getId());
        existingOrNewPump.setPort(pumpPortService.findAllByIdConfiguredAndControllerPtsConfiguration(pump.getPort(), controllerPtsConfiguration));
        existingOrNewPump.setAddress(pump.getAddress());
        existingOrNewPump.setControllerPtsConfiguration(controllerPtsConfiguration);
        return pumpRepository.save(existingOrNewPump);
    }

    public List<Pump> getAll() {
        return pumpRepository.findAll();
    }

    public void update(Long id, Pump pump) {
        Optional<Pump> pumpOptional = pumpRepository.findById(id);
        if (pumpOptional.isPresent()) {
            Pump pump1 = pumpOptional.get();
            pump1.setPort(pump.getPort());

            pumpRepository.save(pump1).getId();
        }
    }

    @Override
    public void delete(Long id) {
        Optional<Pump> probeOptional = pumpRepository.findById(id);
        probeOptional.ifPresent(pumpRepository::delete);
    }

    /**
     * Méthode de map d'un probe en probeConfigDt
     *
     * @param pump
     * @return
     */
    public PumpsConfigDto mapToPumpConfigDto(Pump pump) {
        return PumpsConfigDto.builder()
                .id(pump.getIdConf())
                .address(pump.getAddress().toString())
                .portId(pump.getPort().getIdConfigured().toString())
                .protocol(pump.getPort().getProtocol())
                .baudRate(pump.getPort().getBaudRate()).build();
    }

    public List<Long> findPumpIdsByControllerConfiguration(String controllerPtsConfiguration, String ptsId) {
        return pumpRepository.findPumpIdsByControllerConfiguration(controllerPtsConfiguration,ptsId);
    }

    public Pump findByIdConfAndConfigurationIdAndControllerId(String configurationId, String ptsId, Long idConf) {
        return pumpRepository.findByIdConfAndConfigurationIdAndControllerId(configurationId, ptsId, idConf);
    }

    public Pump findAllByIdConfAndControllerPtsConfiguration(Long idConf, ControllerPtsConfiguration controllerPtsConfiguration) {
        return pumpRepository.findAllByIdConfAndControllerPtsConfiguration(idConf, controllerPtsConfiguration);
    }

    public Optional<Pump> findByIdConfAndControllerPtsConfiguration(Long idConf, ControllerPtsConfiguration controllerPtsConfiguration) {
        return pumpRepository.findByIdConfAndControllerPtsConfiguration(idConf, controllerPtsConfiguration);
    }
}
