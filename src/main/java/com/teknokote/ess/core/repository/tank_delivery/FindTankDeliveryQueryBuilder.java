package com.teknokote.ess.core.repository.tank_delivery;

import com.teknokote.ess.core.model.movements.TankDelivery;
import com.teknokote.ess.dto.PeriodFilterDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FindTankDeliveryQueryBuilder {
    private static final String INIT_SELECT = "SELECT t FROM TankDelivery t ";
    private static final String INIT_SELECT_COUNT = "SELECT count(t) FROM TankDelivery t ";
    private EntityManager entityManager;
    private Pageable pageable;
    private StringBuilder joins = new StringBuilder("");
    private StringBuilder whereClauses = new StringBuilder(" where 1=1 ");
    private Map<String, Object> parameters = new HashMap<>();
    private FindTankDeliveryQueryBuilder(EntityManager entityManager, Pageable pageable)
    {
        this.entityManager = entityManager;
        this.pageable = pageable;
    }

    public static FindTankDeliveryQueryBuilder init(EntityManager entityManager, Pageable pageable)
    {
        return new FindTankDeliveryQueryBuilder(entityManager, pageable);
    }
    public String prepare()
    {
        return INIT_SELECT + joins + whereClauses;
    }
    public String prepareCount()
    {
        return INIT_SELECT_COUNT + joins + whereClauses;
    }
    public Page<TankDelivery> getResults()
    {
        StringBuilder queryBuilder = new StringBuilder(prepare());
        addDefaultSorting(queryBuilder);
        final TypedQuery<TankDelivery> jQuery = entityManager.createQuery(queryBuilder.toString(), TankDelivery.class);
        parameters.forEach(jQuery::setParameter);
        jQuery.setFirstResult((int) pageable.getOffset());
        jQuery.setMaxResults(pageable.getPageSize());
        final List<TankDelivery> resultList = jQuery.getResultList();
        // Count
        final TypedQuery<Long> jQueryCount = entityManager.createQuery(this.prepareCount(), Long.class);
        parameters.forEach(jQueryCount::setParameter);
        Long total= jQueryCount.getSingleResult();
        return new PageImpl<>(resultList, pageable, total);
    }
    private void addDefaultSorting(StringBuilder queryBuilder) {
        // Add sorting by dateTimeStart in descending order
        queryBuilder.append(" ORDER BY t.startDateTime DESC");
    }
    public FindTankDeliveryQueryBuilder addControllerIdClause(Long ctrId)
    {
        whereClauses.append(" and controllerPts.id= :ctrId");
        parameters.put("ctrId", ctrId);
        return this;
    }
    public FindTankDeliveryQueryBuilder addTankIdConfClause(List<Long> tankIdConf)
    {
        if(Objects.isNull(tankIdConf)) return this;
        joins.append(" join t.tank tank ");
        whereClauses.append(" and tank.idConf in (:tankIdConf)");
        parameters.put("tankIdConf", tankIdConf);
        return this;
    }
    public FindTankDeliveryQueryBuilder addDateTimeStartBetweenClause(PeriodFilterDto period)
    {
        if(Objects.isNull(period)) return this;

        if(!Objects.isNull(period.getFrom())){
            whereClauses.append(" AND t.startDateTime >= :periodFrom");
            parameters.put("periodFrom",period.getFrom());
        }

        if(!Objects.isNull(period.getTo())){
            whereClauses.append(" AND t.startDateTime <= :periodTo");
            parameters.put("periodTo",period.getTo());
        }

        return this;
    }

}
