package com.teknokote.ess.core.service.impl;

import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.dao.UserHistoryDao;
import com.teknokote.ess.core.model.EnumActivityType;
import com.teknokote.ess.core.service.UserHistoryService;
import com.teknokote.ess.dto.UserHistoryDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
public class UserHistoryServiceImpl extends GenericCheckedService<Long, UserHistoryDto> implements UserHistoryService
{
    @Autowired
    private ESSValidator<UserHistoryDto> validator;
    @Autowired
    private UserHistoryDao dao;

    @Override
    public List<UserHistoryDto> userLogs(Long userId,LocalDateTime startDate,LocalDateTime endDate) {
        return getDao().findAllById(userId).stream()
                .filter(userHistoryDto ->
                        !userHistoryDto.getActivityType().equals(EnumActivityType.GET) &&
                                userHistoryDto.getAction() != null &&
                                (startDate == null || !userHistoryDto.getActivityDate().isBefore(startDate)) &&
                                (endDate == null || !userHistoryDto.getActivityDate().isAfter(endDate))
                )
                .sorted(Comparator.comparing(UserHistoryDto::getActivityDate).reversed())
                .collect(Collectors.toList());
    }
}
