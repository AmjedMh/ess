package com.teknokote.ess.core.repository.transactions;

import com.teknokote.ess.core.model.movements.PumpTransaction;
import com.teknokote.ess.dto.PeriodFilterDto;
import com.teknokote.ess.dto.RangeFilterDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FindTransactionsQueryBuilder
{
   private static final String INIT_SELECT = "SELECT t FROM PumpTransaction t ";
   private static final String INIT_SELECT_COUNT = "SELECT count(t) FROM PumpTransaction t ";

   private EntityManager entityManager;
   private Pageable pageable;
   private StringBuilder joins = new StringBuilder("");
   private StringBuilder whereClauses = new StringBuilder(" where 1=1 ");
   private Map<String, Object> parameters = new HashMap<>();


   private FindTransactionsQueryBuilder(EntityManager entityManager, Pageable pageable)
   {
      this.entityManager = entityManager;
      this.pageable = pageable;
   }

   public static FindTransactionsQueryBuilder init(EntityManager entityManager, Pageable pageable)
   {
      return new FindTransactionsQueryBuilder(entityManager, pageable);
   }

   public String prepare()
   {
      return INIT_SELECT + joins + whereClauses;
   }
   public String prepareCount()
   {
      return INIT_SELECT_COUNT + joins + whereClauses;
   }

   public Page<PumpTransaction> getResults()
   {
      StringBuilder queryBuilder = new StringBuilder(prepare());
      addDefaultSorting(queryBuilder);
      final TypedQuery<PumpTransaction> jQuery = entityManager.createQuery(queryBuilder.toString(), PumpTransaction.class);
      parameters.forEach(jQuery::setParameter);
      jQuery.setFirstResult((int) pageable.getOffset());
      jQuery.setMaxResults(pageable.getPageSize());
      final List<PumpTransaction> resultList = jQuery.getResultList();
      // Count
      final TypedQuery<Long> jQueryCount = entityManager.createQuery(this.prepareCount(), Long.class);
      parameters.forEach(jQueryCount::setParameter);
      Long total= jQueryCount.getSingleResult();
      return new PageImpl<>(resultList, pageable, total);
   }
   private void addDefaultSorting(StringBuilder queryBuilder) {
      // Add sorting by dateTimeStart in descending order
      queryBuilder.append(" ORDER BY t.dateTimeStart DESC");
   }


   public FindTransactionsQueryBuilder addControllerIdClause(Long ctrId) {
      whereClauses.append(" and t.controllerPts.id = :ctrId");
      parameters.put("ctrId", ctrId);
      return this;
   }


   public FindTransactionsQueryBuilder addPumpIdConfClause(List<Long> pumpIdConf)
   {
      if(Objects.isNull(pumpIdConf)) return this;
      joins.append(" join t.pump pump ");
      whereClauses.append(" and pump.idConf in (:pumpIdConf)");
      parameters.put("pumpIdConf", pumpIdConf);
      return this;
   }

   public FindTransactionsQueryBuilder addFuelGradeIdConfClause(List<Long> fuelGradeIdConf)
   {
      if(Objects.isNull(fuelGradeIdConf)) return this;
      joins.append(" join t.fuelGrade fuelGrade ");
      whereClauses.append(" and fuelGrade.idConf in (:fuelGradeIdConf)");
      parameters.put("fuelGradeIdConf", fuelGradeIdConf);
      return this;
   }

   public FindTransactionsQueryBuilder addVolumeClause(RangeFilterDto volumeRange)
   {
      if(Objects.isNull(volumeRange) ||(Objects.isNull(volumeRange.getMin()) && Objects.isNull(volumeRange.getMax()))) return this;
      whereClauses.append(" and (t.volume>=:minVolume and t.volume<=:maxVolume)");
      parameters.put("minVolume", volumeRange.getMin());
      parameters.put("maxVolume", volumeRange.getMax());
      return this;
   }

   public FindTransactionsQueryBuilder addPumpAttendantClause(List<Long> pumpAttendantIds)
   {
      if(CollectionUtils.isEmpty(pumpAttendantIds)) return this;
      whereClauses.append(" and t.pumpAttendant.id in (:pumpAttendantIds)");
      parameters.put("pumpAttendantIds", pumpAttendantIds);
      return this;
   }

   public FindTransactionsQueryBuilder addDateTimeStartBetweenClause(PeriodFilterDto period)
   {
      if(Objects.isNull(period)) return this;

      if(!Objects.isNull(period.getFrom())){
         whereClauses.append(" AND t.dateTimeStart >= :periodFrom");
         parameters.put("periodFrom",period.getFrom());
      }

      if(!Objects.isNull(period.getTo())){
         whereClauses.append(" AND t.dateTimeStart <= :periodTo");
         parameters.put("periodTo",period.getTo());
      }

      return this;
   }


}
