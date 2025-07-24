package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long>
{
    List<UserHistory> findAllByUserId(Long userId);
}
