package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.movements.ProbeMeasurements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProbeMeasurementsRepository extends JpaRepository<ProbeMeasurements,Long> {
    @Query("select p from ProbeMeasurements p where p.probe = ?1")
    List<ProbeMeasurements> findMesByProbe(Long probe);
    @Query("SELECT t FROM ProbeMeasurements t where t.probe = ?1 ORDER BY t.probe DESC limit 1")
    ProbeMeasurements getLastTankMeasurement(Long tank);
}
