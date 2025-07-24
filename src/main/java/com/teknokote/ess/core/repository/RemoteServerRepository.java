package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.RemoteServerConf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RemoteServerRepository extends JpaRepository<RemoteServerConf, Long> {
}
