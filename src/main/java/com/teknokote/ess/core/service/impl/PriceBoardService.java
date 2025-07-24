package com.teknokote.ess.core.service.impl;


import com.teknokote.ess.core.model.Pricebord;
import com.teknokote.ess.core.service.base.EntityService;
import com.teknokote.pts.client.response.priceboard.PriceBoardStatus;

public interface PriceBoardService extends EntityService<Pricebord, Long>
{

    Pricebord addPriceBoard(PriceBoardStatus pricebord);
}
