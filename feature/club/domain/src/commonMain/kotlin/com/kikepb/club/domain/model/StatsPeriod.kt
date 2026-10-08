package com.kikepb.club.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * Period of the stats classification (spec 015 AC-015-07, backend spec 012 RN-B). The backend counts the
 * completed matches between `from` and `to`, both inclusive, in the club time zone; the rating is never filtered.
 */
enum class StatsPeriod {
    ALL_TIME,
    THIS_YEAR,
    LAST_30_DAYS;

    /** Inclusive date range for [today] (club time zone), or null for every match. */
    fun rangeFor(today: LocalDate): ClosedRange<LocalDate>? = when (this) {
        ALL_TIME -> null
        THIS_YEAR -> LocalDate(today.year, 1, 1)..today
        LAST_30_DAYS -> today.minus(DatePeriod(days = 29))..today
    }
}
