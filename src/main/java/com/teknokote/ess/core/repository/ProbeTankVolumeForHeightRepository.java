package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.ProbeTankVolumeForHeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProbeTankVolumeForHeightRepository extends JpaRepository<ProbeTankVolumeForHeight,Long> {
    @Query("select p from ProbeTankVolumeForHeight p where p.probe = ?1 and p.height=?2")
    List<ProbeTankVolumeForHeight> findvByph(Long probe, float height);

    @Query("select p from ProbeTankVolumeForHeight p where p.controllerPts.id = ?1")
    List<ProbeTankVolumeForHeight> findProbeTankVolumeForHeightByIdController(Long idCtr);
}
