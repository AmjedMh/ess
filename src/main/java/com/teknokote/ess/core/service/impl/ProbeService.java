package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Probe;
import com.teknokote.ess.core.model.configuration.ProbePort;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.repository.ControllePtsRepository;
import com.teknokote.ess.core.repository.ProbePortRepository;
import com.teknokote.ess.core.repository.ProbeRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import com.teknokote.ess.dto.ProbeConfigDto;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSProbe;
import com.teknokote.pts.client.response.configuration.PTSProbePort;
import com.teknokote.pts.client.response.configuration.PTSProbesConfigurationResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSProbesConfigurationResponsePacketData;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class ProbeService extends AbstractEntityService<Probe, Long>
{

    @Autowired
    private ProbePortRepository probePortRepository;
    @Autowired
    private TankService tankService;
    @Autowired
    private ControllePtsRepository controllePtsRepository;
    @Autowired
    private ControllerPtsConfigurationService controllerPtsConfigurationService;
    @Autowired
    private ProbeRepository probeRepository;

    public ProbeService(ProbeRepository dao, ProbePortRepository probePortRepository, TankService tankService, ControllerPtsConfigurationService controllerPtsConfigurationService) {
        super(dao);
        this.probeRepository = dao;
        this.probePortRepository = probePortRepository;
        this.tankService = tankService;
        this.controllerPtsConfigurationService = controllerPtsConfigurationService;
    }

    public void update(Long id, Probe probe) {
        Optional<Probe> probeOptional = probeRepository.findById(id);
        if (probeOptional.isPresent()) {
            Probe probe1 = probeOptional.get();
            probe1.setAddress(probe.getAddress());

            probeRepository.save(probe1);
        }
    }

    public List<Probe> findProbesByControllerOnCurrentConfiguration(Long idCtr) {
        final ControllerPtsConfiguration currentConfigurationOnController = controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr);
        return probeRepository.findProbesByControllerOnCurrentConfiguration(currentConfigurationOnController);
    }

    @Override
    public void delete(Long id) {
        Optional<Probe> probeOptional = probeRepository.findById(id);
        probeOptional.ifPresent(probeRepository::delete);
    }

    public JsonPTSResponse addNewProbePort(JsonPTSResponse response) {
        return response;
    }

    public void addProbes(ControllerPtsConfiguration controllerPtsConfiguration, JsonPTSResponse response) {
        response.getPackets().stream()
                .map(responsePacket -> ((PTSProbesConfigurationResponsePacket) responsePacket).getData())
                .map(PTSProbesConfigurationResponsePacketData::getPorts)
                .flatMap(Collection::stream)
                .forEach(port->addPort(port,controllerPtsConfiguration));
        response.getPackets().stream()
                .map(responsePacket -> ((PTSProbesConfigurationResponsePacket) responsePacket).getData())
                .map(PTSProbesConfigurationResponsePacketData::getProbes)
                .flatMap(Collection::stream)
                .map(probe -> addProbe(probe, controllerPtsConfiguration))
                .forEach(probe -> {});

    }

    public void addPort(PTSProbePort portPb,ControllerPtsConfiguration controllerPtsConfiguration) {
        Optional<ProbePort> p=probePortRepository.findAllByIdConfiguredAndControllerPtsConfiguration(portPb.getId(),controllerPtsConfiguration);
        if ( p.isEmpty())
            p = Optional.of(new ProbePort());
        ProbePort probePort=p.get();
        probePort.setIdConfigured(portPb.getId());
        probePort.setProtocol(portPb.getProtocol());
        probePort.setBaudRate(portPb.getBaudRate());
        probePort.setControllerPtsConfiguration(controllerPtsConfiguration);
        probePortRepository.save(probePort);
    }


    public Probe addProbe(PTSProbe probe, ControllerPtsConfiguration controllerPtsConfiguration) {
        Probe p=probeRepository.findAllByIdConfAndControllerPtsConfiguration(probe.getId(),controllerPtsConfiguration) ;
        if (p== null)
            p = new Probe();
        p.setIdConf(probe.getId());
        p.setPort(probePortRepository.findAllByIdConfiguredAndControllerPtsConfiguration(probe.getPort(),controllerPtsConfiguration).orElse(null));
        /**
         * A noter que les TANKs ont le même ID CONF que les probes
         */
        Tank tank = tankService.findAllByIdConfAndControllerPtsConfiguration(probe.getId(),controllerPtsConfiguration);
        log.info("this tank: " + tank);
        p.setTank(tank);
        p.setAddress(probe.getAddress());
        p.setControllerPtsConfiguration(controllerPtsConfiguration);
        log.info("this probe: " + p);

        return probeRepository.save(p);
    }


    /**
     * Méthode de map d'un probe en probeConfigDt
     *
     * @param probe
     * @return
     */
    public ProbeConfigDto mapToProbeConfigDto(Probe probe) {

        ProbeConfigDto probeConfigDto = new ProbeConfigDto();
        probeConfigDto.setId(probe.getIdConf());
        probeConfigDto.setPortId(probe.getPort().getIdConfigured());
        probeConfigDto.setProtocol(probe.getPort().getProtocol());
        probeConfigDto.setAddress(probe.getAddress().toString());
        probeConfigDto.setBaudRate(probe.getPort().getBaudRate());
        return probeConfigDto;
    }
}
