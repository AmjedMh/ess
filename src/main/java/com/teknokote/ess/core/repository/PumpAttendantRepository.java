package com.teknokote.ess.core.repository;

import com.teknokote.core.dao.JpaActivatableRepository;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PumpAttendantRepository extends JpaActivatableRepository<PumpAttendant, Long> {
    Optional<PumpAttendant> findAllByfirstName(String name);

    @Query("SELECT pa FROM PumpAttendant pa WHERE pa.station.id = :id order by pa.audit.createdDate desc")
    Page<PumpAttendant> findAllByStation(Long id, Pageable pageable);

    @Query("SELECT pa FROM PumpAttendant pa WHERE pa.station.id = :stationId and pa.tag = :tag")
    Optional<PumpAttendant> findByStationAndTag(Long stationId, String tag);

    @Query("SELECT pa FROM PumpAttendant pa WHERE pa.tag = :tag and pa.stationId = :stationId")
    Optional<PumpAttendant> findByTag(String tag, Long stationId);

    @Query("SELECT pa FROM PumpAttendant pa WHERE pa.station.id = :stationId and pa.matricule = :matricule")
    Optional<PumpAttendant> findByStationAndMatricule(Long stationId, String matricule);

    @Query("SELECT pa FROM PumpAttendant pa WHERE pa.id = :id AND pa.stationId = :stationId")
    Optional<PumpAttendant> findByIdAndStationId(Long stationId, Long id);

}
