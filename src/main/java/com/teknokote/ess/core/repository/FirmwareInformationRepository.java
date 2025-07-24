package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.FirmwareInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FirmwareInformationRepository extends JpaRepository<FirmwareInformation, Long> {

    List<FirmwareInformation> findAllByPtsId(String ptsId);
    @Query("SELECT fi FROM FirmwareInformation fi WHERE fi.dateTime = :dateTime")
    FirmwareInformation findByDateTime(String dateTime);
    @Query("SELECT fi FROM FirmwareInformation fi ORDER BY fi.modificationDate DESC limit 1")
    FirmwareInformation getLastDateTimeVersion();

    @Query("select f.dateTime from FirmwareInformation f join f.controllerPts pts where pts.ptsId= :ptsId order by f.modificationDate desc limit 1")
    Optional<String> getLastModifiedFirmwareInformationVersionByPtsId(String ptsId);
    @Query("select f from FirmwareInformation f join f.controllerPts pts where pts.ptsId= :ptsId order by f.modificationDate desc limit 1")
    Optional<FirmwareInformation> getLastModifiedFirmwareInformationVersion(String ptsId);
}
