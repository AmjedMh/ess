package com.teknokote.ess.core.service.Entities;

import com.teknokote.ess.core.repository.transactions.FindTransactionsQueryBuilder;
import com.teknokote.ess.dto.PeriodFilterDto;
import com.teknokote.ess.dto.RangeFilterDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
class FindTransactionsQueryBuilderTest
{

   @Test
   void prepare()
   {
      final String query = FindTransactionsQueryBuilder.init(null, PageRequest.of(1, 10))
         .addControllerIdClause(5L)
         .addPumpIdConfClause(List.of(1L,2L))
         .addVolumeClause(new RangeFilterDto(1L,10L))
         .addDateTimeStartBetweenClause(PeriodFilterDto.of(LocalDateTime.now(),LocalDateTime.now()))
         .addPumpAttendantClause(List.of(1L,3L))
         .prepare();

      log.info(query);
   }

   @Test
   void prepareWhenNoVolumeClause()
   {
      final String query = FindTransactionsQueryBuilder.init(null, PageRequest.of(1, 10))
         .addControllerIdClause(5L)
         .addPumpIdConfClause(List.of(1L,2L))
         .prepare();

      log.info(query);
   }

   @Test
   void prepareWhenNoPumpClause()
   {
      final String query = FindTransactionsQueryBuilder.init(null, PageRequest.of(1, 10))
         .addControllerIdClause(5L)
         .addVolumeClause(new RangeFilterDto(1L,10L))
         .prepare();

      log.info(query);
   }
}
