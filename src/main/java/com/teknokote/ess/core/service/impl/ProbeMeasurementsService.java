package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.movements.ProbeMeasurements;
import com.teknokote.ess.core.repository.ProbeMeasurementsRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import org.springframework.stereotype.Service;

@Service
public class ProbeMeasurementsService extends AbstractEntityService<ProbeMeasurements, Long>
{

    ProbeMeasurementsRepository probeMeasurementsRepository;

    public ProbeMeasurementsService(ProbeMeasurementsRepository dao) {
        super(dao);
        this.probeMeasurementsRepository = dao;
    }
}
