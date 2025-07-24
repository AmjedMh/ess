package com.teknokote.ess.core.repository;

import com.teknokote.core.dao.JpaActivatableRepository;
import com.teknokote.ess.core.model.configuration.Station;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaActivatableRepository<Station, Long> {
    List<Station> findAllByActif(Boolean actif);

    @Query("SELECT ca FROM Station ca WHERE ca.customerAccountId = :customerAccountId AND LOWER(ca.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Station> findByName(String name, Long customerAccountId);

    @Query("SELECT count(*) FROM Station ca WHERE ca.customerAccountId= :customerAccountId")
    int countStations(Long customerAccountId);

    Station findByControllerPts_PtsId(String ptsId);

    Optional<Station> findByIdentifier(String identifier);
}
