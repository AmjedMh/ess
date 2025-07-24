package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.repository.ControllePtsRepository;
import com.teknokote.ess.core.repository.ProbeMeasurementsRepository;
import com.teknokote.ess.core.repository.TankRepository;
import com.teknokote.ess.core.repository.tank_measurement.TankMeasurementRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import com.teknokote.ess.core.service.impl.transactions.TankLevelPerSalesService;
import com.teknokote.ess.dto.TankConfigDto;
import com.teknokote.ess.dto.charts.TankLevelPerSalesChartDto;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSTank;
import com.teknokote.pts.client.response.configuration.PTSTankConfigurationResponsePacket;
import com.teknokote.pts.client.response.configuration.PTSTankConfigurationResponsePacketData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TankService extends AbstractEntityService<Tank, Long> {
    @Autowired
    TankMeasurementServices tankMeasurementServices;
    @Autowired
    TankMeasurementRepository tankMeasurementRepository;
    @Autowired
    private FuelGradesService fuelGradesService;
    @Autowired
    private TankLevelPerSalesService tankLevelPerSalesService;
    @Autowired
    ControllePtsRepository controllePtsRepository;
    @Autowired
    ProbeMeasurementsRepository probeMeasurementsRepository;
    @Autowired
    private TankRepository tankRepository;
    @Autowired
    private ControllerPtsConfigurationService controllerPtsConfigurationService;

    public TankService(TankRepository dao,FuelGradesService fuelGradesService, TankLevelPerSalesService tankLevelPerSalesService, ControllerPtsConfigurationService controllerPtsConfigurationService) {
        super(dao);
        this.tankRepository = dao;
        this.fuelGradesService = fuelGradesService;
        this.tankLevelPerSalesService = tankLevelPerSalesService;
        this.controllerPtsConfigurationService = controllerPtsConfigurationService;
    }

    @Override
    public Tank save(Tank tank) {
        Tank tank1 = new Tank();
        tank1.setHeight(tank.getHeight());
        tank1.setHighProductAlarm(tank.getHighProductAlarm());
        tank1.setCriticalLowProductAlarm(tank.getCriticalLowProductAlarm());
        tank1.setCriticalHighProductAlarm(tank.getCriticalHighProductAlarm());
        tank1.setHighWaterAlarmHeight(tank.getHighWaterAlarmHeight());
        tank1.setLowProductAlarmHeight(tank.getLowProductAlarmHeight());

        return tankRepository.save(tank1);
    }

    public void update(Long id, Tank tank) {
        Optional<Tank> tank1 = tankRepository.findById(id);
        if (tank1.isPresent()) {
            Tank tank2 = tank1.get();
            tank2.setHeight(tank.getHeight());
            tank2.setHighProductAlarm(tank.getHighProductAlarm());
            tank2.setCriticalLowProductAlarm(tank.getCriticalLowProductAlarm());
            tank2.setCriticalHighProductAlarm(tank.getCriticalHighProductAlarm());
            tank2.setHighWaterAlarmHeight(tank.getHighWaterAlarmHeight());
            tank2.setLowProductAlarmHeight(tank.getLowProductAlarmHeight());

            tankRepository.save(tank2).getId();
        }
    }

    public JsonPTSResponse addNewTank(ControllerPtsConfiguration controllerPtsConfiguration, JsonPTSResponse response) {

        response.getPackets().stream()
                .map(responsePacket -> ((PTSTankConfigurationResponsePacket) responsePacket).getData())
                .map(PTSTankConfigurationResponsePacketData::getTanks)
                .flatMap(Collection::stream)
                .map(tank -> addTank(tank, controllerPtsConfiguration))
                .forEach(tank -> {}); // Consume the stream without storing the result

        return response;
    }


    public Tank addTank(PTSTank tank, ControllerPtsConfiguration controllerPtsConfiguration) {
        Tank t = tankRepository.findAllByIdConfAndControllerPtsConfiguration(tank.getId(), controllerPtsConfiguration);
        if (t == null)
            t = new Tank();
        t.setIdConf(tank.getId());
        t.setHeight(tank.getHeight());
        t.setHighProductAlarm(tank.getHighProductAlarmHeight());
        t.setHighWaterAlarmHeight(tank.getHighWaterAlarmHeight());
        t.setCriticalHighProductAlarm(tank.getCriticalHighProductAlarmHeight());
        t.setCriticalLowProductAlarm(tank.getCriticalLowProductAlarmHeight());
        t.setSetStopPumpsAtCriticalLowProductHeight(tank.getStopPumpsAtCriticalLowProductHeight());
        t.setLowProductAlarmHeight(tank.getLowProductAlarmHeight());
        FuelGrade grades = fuelGradesService.findAllByIdConfAndControllerPtsConfiguration(tank.getFuelGradeId(), controllerPtsConfiguration);
        t.setGrade(grades);
        t.setControllerPtsConfiguration(controllerPtsConfiguration);
        return tankRepository.save(t);
    }

    public List<TankLevelPerSalesChartDto> tankLevelChangesChart(Long idCtr, String tank, LocalDateTime startDate, LocalDateTime endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate must not be null");
        }
        return tankLevelPerSalesService.findAllByControllerPtsIdAndTankPeriod(idCtr, tank, startDate, endDate);
    }

    /**
     * Méthode de map d'un probe en probeConfigDt
     *
     * @param tank
     * @return
     */
    public TankConfigDto mapToTankConfigDto(Tank tank) {
        return TankConfigDto.builder()
                .idConf(tank.getIdConf())
                .fuelGradeId(tank.getGrade().getIdConf())
                .height(tank.getHeight())
                .fuelGrade(tank.getGrade().getName())
                .highProductAlarm(tank.getHighProductAlarm())
                .highWaterAlarm(tank.getHighWaterAlarmHeight())
                .criticalHighProductAlarm(tank.getCriticalHighProductAlarm())
                .criticalLowProductAlarm(tank.getCriticalLowProductAlarm())
                .lowProductAlarm(tank.getLowProductAlarmHeight())
                .stopPumpsAtReachingCriticalLowProductHeight(tank.getSetStopPumpsAtCriticalLowProductHeight())
                .build();
    }

    public List<Tank> findTankByControllerOnCurrentConfiguration(Long idCtr) {
        final ControllerPtsConfiguration currentConfigurationOnController = controllerPtsConfigurationService.findCurrentConfigurationOnController(idCtr);
        return tankRepository.findTankByControllerConfiguration(currentConfigurationOnController);
    }

    public Tank findAllByIdConfAndControllerPtsConfiguration(Long idConf, ControllerPtsConfiguration controllerPtsConfiguration) {
        return tankRepository.findAllByIdConfAndControllerPtsConfiguration(idConf, controllerPtsConfiguration);
    }

    /**
     * Finds tank on the configuration of the controller ptsId
     * @param controllerPtsConfiguration
     * @param ptsId
     * @param idConf
     * @return
     */
    public Tank findByIdConfAndControllerPtsConfigurationAndPtsId(ControllerPtsConfiguration controllerPtsConfiguration,String ptsId,Long idConf){
        return tankRepository.findByIdConfAndControllerPtsConfigurationAndPtsId(controllerPtsConfiguration,ptsId,idConf);
    }
}
