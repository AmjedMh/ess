package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Pump;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PumpRepository extends JpaRepository<Pump, Long> {

    Pump findAllByIdConfAndControllerPtsConfiguration(Long idConf, ControllerPtsConfiguration controllerPtsConfiguration);

    Optional<Pump> findByIdConfAndControllerPtsConfiguration(Long idConf, ControllerPtsConfiguration controllerPtsConfiguration);

    Pump findPumpsByIdConf(Long id);

    @Query("select pump from Pump pump where pump.controllerPtsConfiguration.controllerPts.stationId = :stationId and pump.idConf = :idConf")
    Optional<Pump> findByIdConfAndStationId(Long stationId, Long idConf);

    @Query("select pump from Pump pump where pump.controllerPtsConfiguration.ptsId = :ptsId and pump.controllerPtsConfiguration.configurationId= :configurationId and pump.idConf = :idConf")
    Pump findByIdConfAndConfigurationIdAndControllerId(String ptsId, String configurationId, Long idConf);

    Pump findByIdConfAndControllerPtsConfiguration_ConfigurationId(Long idConf, String configurationId);

    @Query("select p from Pump p join p.controllerPtsConfiguration conf where conf= :controllerPtsConfiguration")
    List<Pump> findPumpsByControllerConfiguration(ControllerPtsConfiguration controllerPtsConfiguration);

    @Query("SELECT DISTINCT p.idConf FROM Pump p " +
            "WHERE p.controllerPtsConfiguration.configurationId = :controllerPtsConfiguration and p.controllerPtsConfiguration.ptsId = :ptsId ")
    List<Long> findPumpIdsByControllerConfiguration(String controllerPtsConfiguration, String ptsId);
}
