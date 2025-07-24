package com.teknokote.ess.core.dao.impl.shifts;

import com.teknokote.core.dao.JpaGenericDao;
import com.teknokote.ess.core.dao.mappers.shifts.PaymentMethodMapper;
import com.teknokote.ess.core.dao.shifts.PaymentMethodDao;
import com.teknokote.ess.core.model.shifts.PaymentMethod;
import com.teknokote.ess.core.repository.shifts.PaymentMethodRepository;
import com.teknokote.ess.dto.shifts.PaymentMethodDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Getter
@Setter
public class PaymentMethodDaoImpl extends JpaGenericDao<Long, PaymentMethodDto, PaymentMethod> implements PaymentMethodDao
{
    @Autowired
    private PaymentMethodMapper mapper;
    @Autowired
    private PaymentMethodRepository repository;
    @Override
    public List<PaymentMethodDto> findByStationId(Long stationId) {
        return getRepository().findByStationId(stationId).stream().map(getMapper()::toDto).collect(Collectors.toList());
    }
}
