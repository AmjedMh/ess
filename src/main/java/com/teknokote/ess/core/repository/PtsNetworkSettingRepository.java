package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.PtsNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PtsNetworkSettingRepository extends JpaRepository<PtsNetwork, Long> {
}
