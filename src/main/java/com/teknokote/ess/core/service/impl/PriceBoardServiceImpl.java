package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.Pricebord;
import com.teknokote.ess.core.repository.PricebordRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import com.teknokote.pts.client.response.priceboard.PriceBoardStatus;
import org.springframework.stereotype.Service;

@Service
public class PriceBoardServiceImpl extends AbstractEntityService<Pricebord, Long> implements PriceBoardService {

    private PricebordRepository pricebordRepository;

    public PriceBoardServiceImpl(PricebordRepository dao) {
        super(dao);
        this.pricebordRepository = dao;
    }

    @Override
    public Pricebord addPriceBoard(PriceBoardStatus pricebord) {
        Pricebord pricebord1 =new Pricebord();
        pricebord1.setPrice(pricebord.getPriceBoard());
        pricebord1.setOnline(pricebord.getOnline());
        pricebord1.setError(pricebord.getError());
        return pricebordRepository.save(pricebord1);
    }
}
