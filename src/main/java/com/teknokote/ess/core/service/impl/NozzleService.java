package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.ControllerDao;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.configuration.Nozzle;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.repository.ControllePtsRepository;
import com.teknokote.ess.core.repository.FuelGradesRepository;
import com.teknokote.ess.core.repository.NozzleRepository;
import com.teknokote.ess.core.repository.TankRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import com.teknokote.ess.dto.ControllerPtsDto;
import com.teknokote.ess.dto.NozzelConfigDto;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSPumpNozzles;
import com.teknokote.pts.client.response.configuration.PTSPumpNozzlesResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSPumpNozzlesResponsePacketData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NozzleService extends AbstractEntityService<Nozzle, Long> {
    @Autowired
    private NozzleRepository nozzleRepository;
    @Autowired
    private PumpService pumpService;
    @Autowired
    private FuelGradesRepository gradesRepository;
    @Autowired
    private TankRepository tankRepository;
    @Autowired
    private ControllerDao controllerDao;
    @Autowired
    ControllePtsRepository controllePtsRepository;
    @Autowired
    private ControllerPtsConfigurationService controllerPtsConfigurationService;

    public NozzleService(NozzleRepository dao,TankRepository tankRepository,ControllerDao controllerDao,ControllerPtsConfigurationService controllerPtsConfigurationService, PumpService pumpService ,FuelGradesRepository gradesRepository) {
        super(dao);
        this.nozzleRepository = dao;
        this.tankRepository = tankRepository;
        this.controllerDao = controllerDao;
        this.controllerPtsConfigurationService = controllerPtsConfigurationService;
        this.pumpService = pumpService;
        this.gradesRepository = gradesRepository;
    }
    public List<Long> findAllFuelIdsByConfigurationAndPumpId(Long pumpIdConf,String configurationId,String ptsId){
        return nozzleRepository.findAllFuelIdsByConfigurationAndPumpId(pumpIdConf,configurationId,ptsId);
    }
    public List<String> findAllFuelByConfigurationAndPumpId(Long pumpIdConf,String configurationId,String ptsId){
        return nozzleRepository.findAllFuelByConfigurationAndPumpId(pumpIdConf,configurationId,ptsId);
    }

    public List<Nozzle> findNozzlesByControllerOnCurrentConfiguration(Long idCtr) {
        final ControllerPtsConfiguration currentConfigurationOnController = controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr);
        return nozzleRepository.findNozzlesByControllerConfiguration(currentConfigurationOnController);
    }

    public List<Nozzle> getAll()
    {
        return nozzleRepository.findAll();
    }

    public JsonPTSResponse addNewPumpNozzles(ControllerPtsConfiguration controllerPtsConfiguration, JsonPTSResponse response) {

        response.getPackets().stream()
                .map(responsePacket -> ((PTSPumpNozzlesResponsePacket) responsePacket).getData())
                .map(PTSPumpNozzlesResponsePacketData::getPumpNozzles)
                .flatMap(Collection::stream)
                .map(nozzles -> addNozzle(nozzles, controllerPtsConfiguration))
                .flatMap(Collection::stream)
                .forEach(nozzle -> {});

        return response;
    }

    /**
     * Ajoute un nouveau nozzle depuis la configuration.
     * Deux cas:
     *  1. pas de cuves configuré
     *  2. des cuves configurés
     */
    public List<Nozzle> addNozzle(PTSPumpNozzles pumpNozzle, ControllerPtsConfiguration controllerPtsConfiguration) {
        List<Nozzle> createdNozzles = new ArrayList<>();
        int numberOfFuelGrades = pumpNozzle.getFuelGradeIds().size();
        int nozzleIndex = 0;

        while (nozzleIndex < numberOfFuelGrades) {
            if (pumpNozzle.getFuelGradeIds().get(nozzleIndex) == 0) {
                nozzleIndex++;
                continue;
            }

            Tank tank = null;
            if (!CollectionUtils.isEmpty(pumpNozzle.getTankIds()) &&
                    nozzleIndex < pumpNozzle.getTankIds().size() &&
                    pumpNozzle.getTankIds().get(nozzleIndex) != 0) {
                tank = tankRepository.findAllByIdConfAndControllerPtsConfiguration(
                        pumpNozzle.getTankIds().get(nozzleIndex), controllerPtsConfiguration);
            }

            createdNozzles.add(createOrUpdateNozzle(
                    controllerPtsConfiguration,
                    Long.valueOf(nozzleIndex) + 1L,  // Explicitly cast to long
                    pumpNozzle.getPumpId(),
                    pumpNozzle.getFuelGradeIds().get(nozzleIndex),
                    tank
            ));

            nozzleIndex++;
        }

        return createdNozzles;
    }

    /**
     * Crée un nozzle
     */
    private Nozzle createOrUpdateNozzle( ControllerPtsConfiguration controllerPtsConfiguration, Long nozzleIdConf, Long pumpIdConf,Long fuelGradeIdConf, Tank tank)
    {
        Pump pump= pumpService.findAllByIdConfAndControllerPtsConfiguration(pumpIdConf,controllerPtsConfiguration);
        Nozzle nozzle1= nozzleRepository.findNozzleByIdConfAndControllerPtsConfigurationAndPump(nozzleIdConf, controllerPtsConfiguration, pumpIdConf);
        if (nozzle1 == null) {
            nozzle1 = new Nozzle();
        }
        FuelGrade grades = gradesRepository.findAllByIdConfAndControllerPtsConfiguration(fuelGradeIdConf, controllerPtsConfiguration);
        nozzle1.setIdConf(nozzleIdConf);
        nozzle1.setControllerPts(controllerPtsConfiguration.getControllerPts());
        nozzle1.setControllerPtsConfiguration(controllerPtsConfiguration);
        nozzle1.setPump(pump);
        nozzle1.setGrade(grades);
        nozzle1.setTank(tank);
        return nozzleRepository.save(nozzle1);
    }

    public NozzelConfigDto mapToNozzelconfigDto(Nozzle nozzle) {
        NozzelConfigDto dto = new NozzelConfigDto();
        try {
            dto.setFuelGrad(nozzle.getGrade().getName());
            dto.setFuelId(nozzle.getGrade().getIdConf().toString());
        } catch (Exception e) {
            dto.setFuelGrad("");
        }

        dto.setPump(nozzle.getPump().getIdConf().toString());

        if (nozzle.getTank() != null) {
            dto.setTank(nozzle.getTank().getIdConf().toString());
        }

        dto.setId(nozzle.getId());
        dto.setIdConf(nozzle.getIdConf());

        return dto;
    }
    public Nozzle findNozzleByIdConfAndPump(Long id, Long pumpIdConf, String configurationId, String ptsId){
        return nozzleRepository.findByIdConfAndPumpAndConfigurationIdAndControllerPts(id, pumpIdConf,configurationId,ptsId);
    }
    public List<Nozzle> findNozzleByPump(Long idCtr,Long pumpIdConf){
        final Optional<ControllerPtsDto> optionalControllerPtsDto = controllerDao.findById(idCtr);
        final Optional<List<Nozzle>> nozzles = optionalControllerPtsDto.map(dto -> nozzleRepository.findNozzleByPump(idCtr, dto.getCurrentConfigurationId(), pumpIdConf));
        return nozzles.orElse(List.of());
    }
    public FuelGrade findFuelByNozzleAndPump(Long nozzleIdConf, Long pumpIdConf, String configurationId, String ptsId){
        return nozzleRepository.findFuelByNozzleAndPump(nozzleIdConf,pumpIdConf,configurationId,ptsId);
    }
    public List<FuelGrade> findAllFuelGradesByPump(Long pumpIdConf, String configurationId, String ptsId){
        return nozzleRepository.findAllFuelGradesByPump(pumpIdConf,configurationId,ptsId);
    }
}
