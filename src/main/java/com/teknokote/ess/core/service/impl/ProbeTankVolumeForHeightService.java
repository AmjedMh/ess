package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.ProbeTankVolumeForHeight;
import com.teknokote.ess.core.repository.ProbeTankVolumeForHeightRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProbeTankVolumeForHeightService extends AbstractEntityService<ProbeTankVolumeForHeight, Long>
{

    private ProbeTankVolumeForHeightRepository repository;

    public ProbeTankVolumeForHeightService(ProbeTankVolumeForHeightRepository dao) {
        super(dao);
        this.repository = dao;
    }
}
