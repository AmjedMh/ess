package com.teknokote.ess.core.dao.impl;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.ShiftDao;
import com.teknokote.ess.core.dao.mappers.ShiftMapper;
import com.teknokote.ess.core.model.shifts.Shift;
import com.teknokote.ess.core.repository.ShiftRepository;
import com.teknokote.ess.dto.ShiftDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Getter
@Setter
public class ShiftDaoImpl extends JpaGenericDao<Long, ShiftDto, Shift> implements ShiftDao {
    @Autowired
    private ShiftMapper mapper;
    @Autowired
    private ShiftRepository repository;

    @Override
    public List<ShiftDto> findOffDay() {
        return getRepository().findAllByOffDay(true).stream().map(getMapper()::toDto).toList();
    }
}
