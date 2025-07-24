package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.ProbePort;
import com.teknokote.ess.core.model.configuration.Reader;
import com.teknokote.ess.core.repository.ControllePtsRepository;
import com.teknokote.ess.core.repository.ProbePortRepository;
import com.teknokote.ess.core.repository.ReaderRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import com.teknokote.ess.dto.ReaderDto;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSProbePort;
import com.teknokote.pts.client.response.configuration.PTSReadersConfiguration;
import com.teknokote.pts.client.response.configuration.PTSReadersConfigurationResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSReadersConfigurationResponsePacketData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReaderServiceImpl extends AbstractEntityService<Reader, Long> implements ReaderService {
    @Autowired
    private ReaderRepository readerRepository;
    @Autowired
    private ProbePortRepository probePortRepository;
    @Autowired
    ControllePtsRepository controllePtsRepository;
    @Autowired
    private ControllerPtsConfigurationService controllerPtsConfigurationService;

    public ReaderServiceImpl(ReaderRepository dao, ProbePortRepository probePortRepository) {
        super(dao);
        this.readerRepository = dao;
        this.probePortRepository = probePortRepository;
    }

    public List<Reader> getAll() {
        return readerRepository.findAll();
    }

    @Override
    public JsonPTSResponse addNewReader(ControllerPtsConfiguration controllerPtsConfiguration, JsonPTSResponse response) {

        response.getPackets().stream()
                .map(responsePacket -> ((PTSReadersConfigurationResponsePacket) responsePacket).getData())
                .map(PTSReadersConfigurationResponsePacketData::getPorts)
                .flatMap(Collection::stream)
                .forEach(port -> addPort(port, controllerPtsConfiguration));
        response.getPackets().stream()
                .map(responsePacket -> ((PTSReadersConfigurationResponsePacket) responsePacket).getData())
                .map(PTSReadersConfigurationResponsePacketData::getReaders)
                .flatMap(Collection::stream)
                .map(reader -> addReader(controllerPtsConfiguration, reader))
                .forEach(readers -> {});
        return response;
    }

    public Reader addReader(ControllerPtsConfiguration controllerPtsConfiguration, PTSReadersConfiguration readersConfiguration) {
        Reader reader = new Reader();
        reader.setIdConf(readersConfiguration.getId());
        reader.setPort(probePortRepository.findAllByIdConfiguredAndControllerPtsConfiguration(readersConfiguration.getPort(), controllerPtsConfiguration).orElse(null));
        reader.setAddress(readersConfiguration.getAddress());
        reader.setPumpId(readersConfiguration.getPumpId());
        reader.setControllerPtsConfiguration(controllerPtsConfiguration);
        return readerRepository.save(reader);

    }

    public void addPort(PTSProbePort portPb, ControllerPtsConfiguration controllerPtsConfiguration) {
        Optional<ProbePort> p = probePortRepository.findAllByIdConfiguredAndControllerPtsConfiguration(portPb.getId(), controllerPtsConfiguration);
        if (p.isEmpty())
            p = Optional.of(new ProbePort());
        ProbePort probePort = p.get();
        probePort.setIdConfigured(portPb.getId());
        probePort.setProtocol(portPb.getProtocol());
        probePort.setBaudRate(portPb.getBaudRate());
        probePort.setControllerPtsConfiguration(controllerPtsConfiguration);
        probePortRepository.save(probePort);
    }

    @Override
    public List<Reader> findReadersByControllerOnCurrentConfiguration(Long idCtr) {
        final ControllerPtsConfiguration currentConfigurationOnController = controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr);
        return readerRepository.findReadersByControllerConfiguration(currentConfigurationOnController);
    }

    public ReaderDto mapToReaderConfigDto(Reader reader) {
        ReaderDto readerDto = new ReaderDto();

        readerDto.setId(reader.getId());
        readerDto.setPort(reader.getPort().getIdConfigured());
        readerDto.setAddress(reader.getAddress());
        readerDto.setPumpId(reader.getPumpId());
        readerDto.setProtocol(reader.getPort().getProtocol());
        readerDto.setBaudRate(reader.getPort().getBaudRate());
        return readerDto;

    }
}
