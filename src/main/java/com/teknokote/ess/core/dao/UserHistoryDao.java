package com.teknokote.ess.core.dao;

import com.teknokote.core.dao.BasicDao;
import com.teknokote.ess.dto.UserHistoryDto;

import java.util.List;


public interface UserHistoryDao extends BasicDao<Long, UserHistoryDto>
{
     List<UserHistoryDto> findAllById(Long userId);
}

