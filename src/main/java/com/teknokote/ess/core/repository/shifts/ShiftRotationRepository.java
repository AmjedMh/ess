package com.teknokote.ess.core.repository.shifts;

import com.teknokote.ess.core.model.shifts.ShiftRotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftRotationRepository extends JpaRepository<ShiftRotation, Long>
{
    @Query("SELECT r FROM ShiftRotation r WHERE r.station.id = :id")
    List<ShiftRotation> findAllByStation(Long id);

   @Query("SELECT r FROM ShiftRotation r WHERE r.station.id = :stationId and ( :month between r.startValidityDate and r.endValidityDate)")
   List<ShiftRotation> findValidRotation(Long stationId, LocalDate month);
    @Query("SELECT r FROM ShiftRotation r WHERE r.station.controllerPts.ptsId = :ptsId and ( :month between r.startValidityDate and r.endValidityDate)")
    ShiftRotation findRotationForStation(String ptsId, LocalDate month);
}
