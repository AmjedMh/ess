package com.teknokote.ess.core.repository.configuration;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ControllerPtsConfigurationRepository extends JpaRepository<ControllerPtsConfiguration, Long> {
    @Query("select c from ControllerPtsConfiguration c where c.ptsId = :ptsId and c.configurationId = :configurationId")
    Optional<ControllerPtsConfiguration> findByPtsIdAndConfigurationId(String ptsId, String configurationId);

    @Query("select conf from ControllerPtsConfiguration conf join conf.controllerPts ctr where ctr.id = :controllerId order by conf.lastConfigurationUpdate desc limit 1")
    Optional<ControllerPtsConfiguration> findCurrentConfigurationOnController(Long controllerId);

    @Query("select conf.configurationId from ControllerPtsConfiguration conf join conf.controllerPts ctr where ctr.ptsId = :ptsId order by conf.lastConfigurationUpdate desc limit 1")
    String findCurrentConfigurationByPtsId(String ptsId);
}
