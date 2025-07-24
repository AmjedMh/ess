package com.teknokote.ess.core.service.shifts;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
@Slf4j
public class Plan
{
   public static void main(String[] args)
   {

      // configuration
      int nbFreeDays = 2;
      int nbPost = 3;
      List<String> teams = Arrays.asList("A", "B", "C", "D", "E");
      int month = 1;

      LocalDate firstDayOfMonth = LocalDate.now().withMonth(month).withDayOfMonth(1);
      LocalDate lastDayOfMonth = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth());

      Map<Integer, List<String>> planning = new HashMap<>();

      int freeDayIndex = 1;
      int changeIndex = 1;

      // init first day configuration
      planning.put(1, new ArrayList<>(teams));

      for(int day = firstDayOfMonth.getDayOfMonth() + 1; day <= lastDayOfMonth.getDayOfMonth(); day++)
      {

         List<String> lastDayPlanning = new ArrayList<>(planning.get(day - 1));

         freeDayIndex++;

         if(freeDayIndex > nbFreeDays)
         {
            // change planning
            String freeTeam = lastDayPlanning.get(3);
            lastDayPlanning.set(3, lastDayPlanning.get(changeIndex - 1));
            lastDayPlanning.set(changeIndex - 1, freeTeam);

            //update change index
            changeIndex++;
            if(changeIndex > nbPost) changeIndex = 1;

            // increment freeday count
            freeDayIndex = 1;

         }

         planning.put(day, new ArrayList<>(lastDayPlanning));
      }

      planning.forEach((day, team) -> log.info(day + " " + team));

   }
}
