package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.ProbePort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProbePortRepository extends JpaRepository<ProbePort,Long> {

    Optional<ProbePort> findAllByIdConfiguredAndControllerPtsConfiguration(String idConf, ControllerPtsConfiguration controllerPtsConfiguration);

}
