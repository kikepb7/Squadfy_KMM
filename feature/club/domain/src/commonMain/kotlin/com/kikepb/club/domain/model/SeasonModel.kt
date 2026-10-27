package com.kikepb.club.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/** Spec 020 APP-RN-S1/S5: before its start, in progress, or closed with its standings frozen. */
enum class SeasonStatus { UPCOMING, ACTIVE, CLOSED }

/** A season of the club (spec 020): from a day of September to a day of June of the next year. */
data class SeasonModel(
    val id: String,
    /** «2026/27» */
    val name: String,
    val startsOn: LocalDate,
    val endsOn: LocalDate,
    val status: SeasonStatus
) {
    val startYear: Int get() = startsOn.year
    val isEditable: Boolean get() = status != SeasonStatus.CLOSED
}

/**
 * Spec 020 APP-RN-S3 (UX only, the server validates): a season starts on a day of September of its year and ends on a
 * day of June of the next one.
 */
object SeasonDatesPolicy {
    val START_MONTH = Month.SEPTEMBER
    val END_MONTH = Month.JUNE
    const val LAST_DAY = 30

    fun isValid(startYear: Int, startsOn: LocalDate, endsOn: LocalDate): Boolean =
        startsOn.year == startYear && startsOn.month == START_MONTH &&
            endsOn.year == startYear + 1 && endsOn.month == END_MONTH

    fun startOn(startYear: Int, day: Int): LocalDate = LocalDate(startYear, START_MONTH, day.coerceIn(1, LAST_DAY))
    fun endOn(startYear: Int, day: Int): LocalDate = LocalDate(startYear + 1, END_MONTH, day.coerceIn(1, LAST_DAY))
}

/** The season the standings show first: the one in progress, else the next one (pre-season), else the newest. */
fun List<SeasonModel>.current(): SeasonModel? =
    firstOrNull { it.status == SeasonStatus.ACTIVE } ?: firstOrNull { it.status == SeasonStatus.UPCOMING } ?: firstOrNull()
