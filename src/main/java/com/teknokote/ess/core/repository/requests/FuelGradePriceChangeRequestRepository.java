package com.teknokote.ess.core.repository.requests;

import com.teknokote.ess.core.model.requests.EnumRequestStatus;
import com.teknokote.ess.core.model.requests.FuelGradePriceChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelGradePriceChangeRequestRepository extends JpaRepository<FuelGradePriceChangeRequest, Long>
{
   @Query("SELECT fp FROM FuelGradePriceChangeRequest fp WHERE fp.station.id = :stationId and fp.status = 'EXECUTED'")
   List<FuelGradePriceChangeRequest> findExecutedByStation(Long stationId);
   @Query("SELECT fp FROM FuelGradePriceChangeRequest fp WHERE fp.station.id = :stationId and fp.id = :id")
   FuelGradePriceChangeRequest findByStation(Long stationId,Long id);

   @Query("SELECT fp FROM FuelGradePriceChangeRequest fp WHERE fp.status in (:requestStatuses)")
   List<FuelGradePriceChangeRequest> findAllByStatusAndReachedPlannedDate(List<EnumRequestStatus> requestStatuses);

   @Query("SELECT fp FROM FuelGradePriceChangeRequest fp WHERE fp.station.id = :stationId")
   List<FuelGradePriceChangeRequest> findPlannedByStation(Long stationId);
}
