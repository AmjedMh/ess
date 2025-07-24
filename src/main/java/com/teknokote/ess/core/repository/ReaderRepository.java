package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReaderRepository extends JpaRepository<Reader,Long>{

    @Query("select p from Reader p where p.controllerPtsConfiguration= :controllerPtsConfiguration")
    List<Reader> findReadersByControllerConfiguration(ControllerPtsConfiguration controllerPtsConfiguration);

}
