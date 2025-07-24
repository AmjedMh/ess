package com.teknokote.ess.core.repository.tank_measurement;

import com.teknokote.ess.core.model.movements.TankMeasurement;
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

public class FindTankMeasurementQueryBuilder {
    private static final String INIT_SELECT = "SELECT t FROM TankMeasurement t ";
    private static final String INIT_SELECT_COUNT = "SELECT count(t) FROM TankMeasurement t ";
    private EntityManager entityManager;
    private Pageable pageable;
    private StringBuilder joins = new StringBuilder("");
    private StringBuilder whereClauses = new StringBuilder(" where 1=1 ");
    private Map<String, Object> parameters = new HashMap<>();
    private FindTankMeasurementQueryBuilder(EntityManager entityManager, Pageable pageable)
    {
        this.entityManager = entityManager;
        this.pageable = pageable;
    }

    public static FindTankMeasurementQueryBuilder init(EntityManager entityManager, Pageable pageable)
    {
        return new FindTankMeasurementQueryBuilder(entityManager, pageable);
    }
    public String prepare()
    {
        return INIT_SELECT + joins + whereClauses;
    }
    public String prepareCount()
    {
        return INIT_SELECT_COUNT + joins + whereClauses;
    }
    public Page<TankMeasurement> getResults()
    {
        StringBuilder queryBuilder = new StringBuilder(prepare());
        addDefaultSorting(queryBuilder);
        final TypedQuery<TankMeasurement> jQuery = entityManager.createQuery(queryBuilder.toString(), TankMeasurement.class);
        parameters.forEach(jQuery::setParameter);
        jQuery.setFirstResult((int) pageable.getOffset());
        jQuery.setMaxResults(pageable.getPageSize());
        final List<TankMeasurement> resultList = jQuery.getResultList();
        // Count
        final TypedQuery<Long> jQueryCount = entityManager.createQuery(this.prepareCount(), Long.class);
        parameters.forEach(jQueryCount::setParameter);
        Long total= jQueryCount.getSingleResult();
        return new PageImpl<>(resultList, pageable, total);
    }
    private void addDefaultSorting(StringBuilder queryBuilder) {
        // Add sorting by dateTimeStart in descending order
        queryBuilder.append(" ORDER BY t.dateTime DESC");
    }
    public FindTankMeasurementQueryBuilder addControllerIdClause(Long ctrId)
    {
        whereClauses.append(" and controllerPts.id= :ctrId");
        parameters.put("ctrId", ctrId);
        return this;
    }
    public FindTankMeasurementQueryBuilder addTankIdConfClause(List<Long> tankId)
    {
        if(Objects.isNull(tankId)) return this;
        whereClauses.append(" and t.tank in (:tankId)");
        parameters.put("tankId", tankId);
        return this;
    }
    public FindTankMeasurementQueryBuilder addDateTimeStartBetweenClause(PeriodFilterDto period)
    {
        if(Objects.isNull(period)) return this;

        if(!Objects.isNull(period.getFrom())){
            whereClauses.append(" AND t.dateTime >= :periodFrom");
            parameters.put("periodFrom",period.getFrom());
        }

        if(!Objects.isNull(period.getTo())){
            whereClauses.append(" AND t.dateTime <= :periodTo");
            parameters.put("periodTo",period.getTo());
        }

        return this;
    }

}

