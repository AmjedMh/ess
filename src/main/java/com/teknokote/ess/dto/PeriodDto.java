package com.teknokote.ess.dto;

import com.teknokote.ess.core.model.movements.EnumFilter;
import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Data
@Builder
public class PeriodDto {
    private EnumFilter periodType;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public PeriodDto(EnumFilter periodType, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.periodType = periodType;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public static PeriodDto today(LocalDateTime dayStart, LocalDateTime dayEnd) {
        return PeriodDto.builder().periodType(EnumFilter.TODAY)
                .startDateTime(dayStart)
                .endDateTime(dayEnd).build();
    }

    public PeriodDto yesterday() {
        final LocalDateTime startDateTime1 = LocalDateTime.of(this.getStartDateTime().toLocalDate(),this.getStartDateTime().toLocalTime());
        final LocalDateTime endDateTime1 = LocalDateTime.of(this.getEndDateTime().toLocalDate(),this.getEndDateTime().toLocalTime());
        return PeriodDto.builder().periodType(EnumFilter.YESTERDAY)
                .startDateTime(startDateTime1.minusDays(1))
                .endDateTime(endDateTime1.minusDays(1)).build();
    }

    public PeriodDto week() {
        final LocalDateTime startDateTime1 = LocalDateTime.of(this.getStartDateTime().toLocalDate(),this.getStartDateTime().toLocalTime());
        final LocalDateTime endDateTime1 = LocalDateTime.of(this.getEndDateTime().toLocalDate(),this.getEndDateTime().toLocalTime());
        boolean isEndTimeAtEndOfDay = this.endDateTime.toLocalTime().equals(LocalDateTime.of(1, 1, 1, 23, 59,59).toLocalTime()) || this.endDateTime.toLocalDate().equals(this.startDateTime.toLocalDate());
        long daysToAdd = isEndTimeAtEndOfDay ? 6 : 7;
        LocalDateTime weekEndDateTime = endDateTime1.with(DayOfWeek.MONDAY).plusDays(daysToAdd);
        return PeriodDto.builder().periodType(EnumFilter.WEEKLY)
                .startDateTime(startDateTime1.with(DayOfWeek.MONDAY))
                .endDateTime(weekEndDateTime)
                .build();
    }

    public PeriodDto month() {
        final LocalDateTime startDateTime1 = LocalDateTime.of(this.getStartDateTime().toLocalDate(),this.getStartDateTime().toLocalTime());
        final LocalDateTime endDateTime1 = LocalDateTime.of(this.getEndDateTime().toLocalDate(),this.getEndDateTime().toLocalTime());
        boolean isEndTimeAtEndOfDay = this.endDateTime.toLocalTime().equals(LocalDateTime.of(1, 1, 1, 23, 59,59).toLocalTime()) || this.endDateTime.toLocalDate().equals(this.startDateTime.toLocalDate()) ;

        long minusDay = isEndTimeAtEndOfDay ? 1:0;
        return PeriodDto.builder().periodType(EnumFilter.MONTHLY)
                .startDateTime(startDateTime1.withDayOfMonth(1))
                .endDateTime(endDateTime1.withDayOfMonth(1).plusMonths(1).minusDays(minusDay))
                .build();
    }

    public PeriodDto year() {
        final LocalDateTime startDateTime1 = LocalDateTime.of(this.getStartDateTime().toLocalDate(),this.getStartDateTime().toLocalTime());
        final LocalDateTime endDateTime1 = LocalDateTime.of(this.getEndDateTime().toLocalDate(),this.getEndDateTime().toLocalTime());
        boolean isEndTimeAtEndOfDay = this.endDateTime.toLocalTime().equals(LocalDateTime.of(1, 1, 1, 23, 59,59).toLocalTime()) || this.endDateTime.toLocalDate().equals(this.startDateTime.toLocalDate());
        long minusDay = isEndTimeAtEndOfDay ? 1:0;
        return PeriodDto.builder().periodType(EnumFilter.YEARLY)
                .startDateTime(startDateTime1.withDayOfYear(1).withMonth(1))
                .endDateTime(endDateTime1.plusYears(1).withDayOfYear(1).minusDays(minusDay))
                .build();
    }
}
