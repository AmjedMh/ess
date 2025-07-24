package com.teknokote.ess.core.repository.organization;

import com.teknokote.ess.core.model.organization.PumpAttendantTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PumpAttendantTeamRepository extends JpaRepository<PumpAttendantTeam, Long> {
    @Query("SELECT pa FROM PumpAttendantTeam pa WHERE pa.station.id = :id")
    List<PumpAttendantTeam> findAllByStation(Long id);

    @Query("SELECT sp.pumpAttendantTeam FROM ShiftPlanning sp WHERE sp.shiftId = :shiftId AND sp.shiftRotationId = :shiftRotationId")
    PumpAttendantTeam findByShiftIdAndShiftRotationId(Long shiftId, Long shiftRotationId);

    @Query("SELECT af.pumpAttendantTeam FROM AffectedPumpAttendant af WHERE af.pumpAttendantId = :pumpAttendantId")
    List<PumpAttendantTeam> findByPumpAttendant(Long pumpAttendantId);
    @Query("SELECT pat FROM PumpAttendantTeam pat " +
            "JOIN pat.affectedPumpAttendant apa " +
            "WHERE pat.shiftRotationId = :shiftRotationId " +
            "AND apa.pumpAttendantId = :pumpAttendantId")
    List<PumpAttendantTeam> findTeamsByRotationAndPumpAttendant(Long shiftRotationId, Long pumpAttendantId);

    @Query("SELECT pa FROM PumpAttendantTeam pa WHERE pa.station.id = :stationId and pa.shiftRotation.id = :shiftRotationId")
    List<PumpAttendantTeam> findByStationAndRotation(Long stationId, Long shiftRotationId);
    @Query("SELECT pa FROM PumpAttendantTeam pa WHERE pa.station.id = :stationId and pa.shiftRotation.id = :shiftRotationId and pa.name = :name")
    Optional<PumpAttendantTeam> findByRotationAndName(Long stationId, Long shiftRotationId, String name);
}
