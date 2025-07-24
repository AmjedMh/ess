package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.dao.ShiftDao;
import com.teknokote.ess.core.service.ShiftService;
import com.teknokote.ess.dto.ShiftDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Getter
public class ShiftServiceImpl extends GenericCheckedService<Long, ShiftDto> implements ShiftService
{
    @Autowired
    private ESSValidator<ShiftDto> validator;
    @Autowired
    private ShiftDao dao;

    @Override
    public ShiftDto findOffDay()
    {
        final List<ShiftDto> offDayShifts = getDao().findOffDay();
        if(offDayShifts.isEmpty()){
            throw new ServiceValidationException("There is no defined off day shift");
        }
        if(offDayShifts.size()>1)
        {
            throw new ServiceValidationException("There is more one defined off day shift");
        }
        return offDayShifts.get(0);
    }
}
