package com.teknokote.ess.core.dao.impl;

import com.teknokote.ess.core.dao.UserHistoryDao;
import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.UserHistoryMapper;
import com.teknokote.ess.core.model.UserHistory;
import com.teknokote.ess.core.repository.UserHistoryRepository;
import com.teknokote.ess.dto.UserHistoryDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Getter
@Setter
public class UserHistoryDaoImpl extends JpaGenericDao<Long,UserHistoryDto, UserHistory> implements UserHistoryDao
{
    @Autowired
    private UserHistoryMapper mapper;
    @Autowired
    private UserHistoryRepository repository;

    @Override
    public List<UserHistoryDto> findAllById(Long userId) {
        return getRepository().findAllByUserId(userId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }
}
