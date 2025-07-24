package com.teknokote.ess.core.service;

import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.UserHistoryDto;

import java.time.LocalDateTime;
import java.util.List;

public interface UserHistoryService extends BaseService<Long, UserHistoryDto>
{

    List<UserHistoryDto> userLogs(Long userId,LocalDateTime startDate, LocalDateTime endDate);
}
