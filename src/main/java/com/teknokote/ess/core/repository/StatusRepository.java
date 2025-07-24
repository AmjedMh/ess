package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.StatusModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<StatusModel,Long> {
        @Query("SELECT t FROM StatusModel t ORDER BY t.id DESC limit 1 ")
        StatusModel getLastStatus();
}
