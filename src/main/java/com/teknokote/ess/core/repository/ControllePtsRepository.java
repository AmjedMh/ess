package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.configuration.ControllerPts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ControllePtsRepository extends JpaRepository<ControllerPts,Long> {

    @Query("select c from ControllerPts c where c.ptsId = ?1")
    Optional<ControllerPts> findControllerPtsById(String ptsId);
}
