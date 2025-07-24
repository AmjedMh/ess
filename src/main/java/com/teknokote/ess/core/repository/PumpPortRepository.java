package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.PumpPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PumpPortRepository extends JpaRepository<PumpPort,Long> {
    Optional<PumpPort> findAllByIdConfiguredAndControllerPtsConfiguration(Long idConf, ControllerPtsConfiguration controllerPtsConfiguration);
}
