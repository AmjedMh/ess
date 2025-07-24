package com.teknokote.ess.core.service.impl;


import com.teknokote.ess.core.model.PumpPrice;
import com.teknokote.ess.core.repository.PumpPricesRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import org.springframework.stereotype.Service;

@Service
public class PumpPriceService extends AbstractEntityService<PumpPrice, Long>
{

    PumpPricesRepository pumpPricesRepository;

    public PumpPriceService(PumpPricesRepository dao) {
        super(dao);
        this.pumpPricesRepository = dao;
    }
}
